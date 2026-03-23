package com.dev.thesis_management.organization.service;

import com.dev.thesis_management.file_asset.service.FileService;
import com.dev.thesis_management.infra.storage.StorageService;
import com.dev.thesis_management.organization.dto.OrgResponse;
import com.dev.thesis_management.organization.dto.StructureResponse;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.dto.UpdateOrgRequest;
import com.dev.thesis_management.organization.mapper.CollegeMapper;
import com.dev.thesis_management.organization.mapper.DepartmentMapper;
import com.dev.thesis_management.organization.mapper.FacultyMapper;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.repository.OrganizationRepository;
import com.dev.thesis_management.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.common.utils.UUIDUtils.*;
import static com.dev.thesis_management.organization.mapper.OrgMapper.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrgService {
    OrganizationRepository organizationRepository;

    UserService userService;
    StorageService storageService;
    FileService fileAssetService;

    // PUBLIC - GET ORG BY CODE
    public OrgResponse getOrgByCode(String code){
        return toResponse(findByCode(code));
    }

    // MANAGER - GET CURRENT ORG
    public OrgResponse getCurrentOrg(UUID userId) {
        return toResponse(findByUserId(userId));
    }

    // MANAGER - UPDATE ORG INFO
    public OrgResponse updateOrgInfo(UUID userId, UpdateOrgRequest request){
        Organization org = userService.getUserById(userId).getOrganization();
        System.out.println("Address: "+request.getAddress());
        updateOrgFromRequest(org, request);
        return toResponse(organizationRepository.save(org));
    }

    // MANAGER - UPDATE ORG MEDIA
    public OrgResponse updateOrgMedia(UUID userId,
                                      MultipartFile logo,
                                      MultipartFile cover,
                                      MultipartFile banner){
        Organization org = userService.getUserById(userId).getOrganization();
        if(!org.getManager().getId().equals(userId)){
            throw new RuntimeException();
        }

        if (logo != null && !logo.isEmpty()) {
            if(org.getLogo() != null){
                fileAssetService.delete(org.getLogo().getId(), userId);
            }
            org.setLogo(fileAssetService.upload(logo, userId));
        }

        if (cover != null && !cover.isEmpty()) {
            if(org.getCover() != null){
                fileAssetService.delete(org.getCover().getId(), userId);
            }
            org.setCover(fileAssetService.upload(cover, userId));
        }

        if (banner != null && !banner.isEmpty()) {
            if(org.getBanner() != null){
                fileAssetService.delete(org.getBanner().getId(), userId);
            }
            org.setBanner(fileAssetService.upload(banner, userId));
        }

        return toResponse(organizationRepository.save(org));
    }

    public StructureResponse getCurrentOrgStructure(UUID uuid) {
        Organization org = findByUserId(uuid);
        return StructureResponse.builder()
                .departments(org.getDepartments().stream()
                        .filter(department -> department.getFaculty() == null)
                        .map(DepartmentMapper::toDepartmentResponse)
                        .toList())
                .faculties(org.getFaculties()
                        .stream()
                        .filter(fac -> fac.getCollege() == null)
                        .map(FacultyMapper::facultyToResponse)
                        .toList())
                .colleges(org.getColleges().stream().map(CollegeMapper::collegeToResponse).toList())
                .build();
    }

    /*********** HELPER METHODS **********/

    // FIND BY CODE
    public Organization findByCode(String code){
        return organizationRepository.findByIdOrCode(parseUUID(code), code).orElseThrow();
    }

    // FIND BY ID
    public Organization findOrgById(UUID orgId){
        return  organizationRepository.findById(orgId).orElseThrow();
    }

    // FIND BY USER ID
    public Organization findByUserId(UUID userId){
        User user = userService.getUserById(userId);
        return switch (user.getRole()) {
            case MANAGER -> {
                if (user.getOrganization() == null) {
                    throw new IllegalStateException("Manager has no organization assigned");
                }
                yield user.getOrganization();
            }
            case STUDENT -> {
                if (user.getStudent() == null || user.getStudent().getOrganization() == null) {
                    throw new IllegalStateException("Student has no organization assigned");
                }
                yield user.getStudent().getOrganization(); }
            case LECTURER -> {
                if (user.getLecturer() == null || user.getLecturer().getOrganization() == null) {
                    throw new IllegalStateException("Lecturer has no organization assigned");
                }
                yield user.getLecturer().getOrganization();
            }
            default -> throw new UnsupportedOperationException(
                    "Role not supported: " + user.getRole()
            );
        };
    }

    // ENRICH ORG
    public Organization enrichOrgWithUrls(Organization org){
        if (org.getLogo() != null) {
            org.getLogo().setUrl(
                    storageService.generatePresignedUrl(org.getLogo().getPath())
            );
        }
        if (org.getCover() != null) {
            org.getCover().setUrl(
                    storageService.generatePresignedUrl(org.getCover().getPath())
            );
        }
        if (org.getBanner() != null) {
            org.getBanner().setUrl(
                    storageService.generatePresignedUrl(org.getBanner().getPath())
            );
        }
        return org;
    }

    // ORG -> ENRICH -> RESPONSE
    OrgResponse toResponse(Organization org){
        return toOrgInfoResponse(enrichOrgWithUrls(org));
    }
}
