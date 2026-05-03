package com.dev.thesis_management.organization.service;

import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.service.FileService;
import com.dev.thesis_management.infra.storage.StorageService;
import com.dev.thesis_management.organization.dto.OrgResponse;
import com.dev.thesis_management.organization.dto.StructureResponse;
import com.dev.thesis_management.organization.dto.organization.OrganizationSearchForm;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.dto.UpdateOrgRequest;
import com.dev.thesis_management.organization.mapper.CollegeMapper;
import com.dev.thesis_management.organization.mapper.DepartmentMapper;
import com.dev.thesis_management.organization.mapper.FacultyMapper;
import com.dev.thesis_management.specifications.OrganizationSpecification;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.repository.OrganizationRepository;
import com.dev.thesis_management.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.UUIDUtils.*;
import static com.dev.thesis_management.organization.mapper.OrgMapper.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrgService {

    OrganizationRepository organizationRepository;
    UserService userService;
    StorageService storageService;
    FileService fileAssetService;

    /* ================= PUBLIC ================= */

    public OrgResponse getOrgByCode(String code) {
        Organization org = findByCode(code);
        return toResponse(org);
    }

    public Page<OrgResponse> searchOrganization(OrganizationSearchForm form, Pageable pageable){
        Specification<Organization> spec =
                OrganizationSpecification.build(form);

        return organizationRepository
                .findAll(spec, pageable)
                .map(this::toResponse);
    }

    public OrgResponse getCurrentOrg(UUID userId) {
        User user = userService.getUserById(userId);

        Organization org = findByUserId(userId);

        OrgResponse response = toResponse(org);
        response.setRole(user.getRole().name());

        return response;
    }

    public OrgResponse updateOrgInfo(UUID userId, UpdateOrgRequest request) {

        Organization org = findByUserId(userId);

        validateManager(org, userId);

        updateOrgFromRequest(org, request);

        return toResponse(organizationRepository.save(org));
    }

    public OrgResponse updateOrgMedia(
            UUID userId,
            MultipartFile logo,
            MultipartFile cover,
            MultipartFile banner
    ) {

        Organization org = findByUserId(userId);

        validateManager(org, userId);

        updateMedia(org, logo, userId, MediaType.LOGO);
        updateMedia(org, cover, userId, MediaType.COVER);
        updateMedia(org, banner, userId, MediaType.BANNER);

        return toResponse(organizationRepository.save(org));
    }

    public StructureResponse getCurrentOrgStructure(UUID userId) {

        Organization org = findByUserId(userId);

        return StructureResponse.builder()
                .departments(org.getDepartments().stream()
                        .filter(d -> d.getFaculty() == null)
                        .map(DepartmentMapper::toDepartmentResponse)
                        .toList())

                .faculties(org.getFaculties().stream()
                        .filter(f -> f.getCollege() == null)
                        .map(FacultyMapper::facultyToResponse)
                        .toList())

                .colleges(org.getColleges().stream()
                        .map(CollegeMapper::collegeToResponse)
                        .toList())
                .build();
    }

    /* ================= FIND ================= */

    public Organization findByCode(String code) {
        return organizationRepository
                .findByIdOrCode(parseUUID(code), code)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found"));
    }

    public Organization findOrgById(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found"));
    }

    public Organization findByUserId(UUID userId) {

        User user = userService.getUserById(userId);

        return switch (user.getRole()) {

            case MANAGER -> requireOrg(user.getOrganization());

            case STUDENT -> requireOrg(
                    user.getStudent().getOrganization()
            );

            case LECTURER -> requireOrg(
                    user.getLecturer().getOrganization()
            );

            default -> throw new RuntimeException(
                    "Role not supported: " + user.getRole()
            );
        };
    }

    /* ================= HELPER ================= */

    private Organization requireOrg(Organization org) {

        if (org == null)
            throw new RuntimeException("Organization not assigned");

        return org;
    }

    private void validateManager(Organization org, UUID userId) {

        if (!org.getManager().getId().equals(userId))
            throw new RuntimeException("Only manager can update organization");
    }

    private void updateMedia(
            Organization org,
            MultipartFile file,
            UUID userId,
            MediaType type
    ) {

        if (file == null || file.isEmpty())
            return;

        switch (type) {

            case LOGO -> {
                deleteIfExists(org.getLogo(), userId);
                org.setLogo(fileAssetService.upload(file, userId));
            }

            case COVER -> {
                deleteIfExists(org.getCover(), userId);
                org.setCover(fileAssetService.upload(file, userId));
            }

            case BANNER -> {
                deleteIfExists(org.getBanner(), userId);
                org.setBanner(fileAssetService.upload(file, userId));
            }
        }
    }

    private void deleteIfExists(FileAsset file, UUID userId) {

        if (file != null)
            fileAssetService.delete(file.getId(), userId);
    }

    private Organization enrichOrgWithUrls(Organization org) {

        if (org.getLogo() != null)
            org.getLogo().setUrl(
                    storageService.generatePresignedUrl(
                            org.getLogo().getPath()
                    )
            );

        if (org.getCover() != null)
            org.getCover().setUrl(
                    storageService.generatePresignedUrl(
                            org.getCover().getPath()
                    )
            );

        if (org.getBanner() != null)
            org.getBanner().setUrl(
                    storageService.generatePresignedUrl(
                            org.getBanner().getPath()
                    )
            );

        return org;
    }

    private OrgResponse toResponse(Organization org) {
        return toOrgInfoResponse(enrichOrgWithUrls(org));
    }

    /* ================= ENUM ================= */

    private enum MediaType {
        LOGO,
        COVER,
        BANNER
    }
}
