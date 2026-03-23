package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.specifications.CouncilSpecification;
import com.dev.thesis_management.thesis.dto.council.CouncilMemberRequest;
import com.dev.thesis_management.thesis.dto.council.CouncilRequest;
import com.dev.thesis_management.thesis.dto.council.CouncilResponse;
import com.dev.thesis_management.thesis.dto.council.CouncilSearchForm;
import com.dev.thesis_management.thesis.entity.Council;
import com.dev.thesis_management.thesis.entity.CouncilMember;
import com.dev.thesis_management.thesis.entity.CouncilRole;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.mapper.CouncilMapper;
import com.dev.thesis_management.thesis.repository.CouncilMemberRepository;
import com.dev.thesis_management.thesis.repository.CouncilRepository;
import com.dev.thesis_management.thesis.repository.CouncilRoleRepository;
import com.dev.thesis_management.thesis.repository.SemesterRepository;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.repository.LecturerRepository;
import com.dev.thesis_management.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.dev.thesis_management.thesis.mapper.CouncilMapper.toCouncilResponse;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CouncilService {

    SemesterRepository semesterRepository;
    CouncilRoleRepository councilRoleRepository;
    CouncilRepository councilRepository;

    OrgService orgService;
    LecturerRepository lecturerRepository;
    UserRepository userRepository;
    CouncilMemberRepository councilMemberRepository;

    /* =========================================================
                        COUNCILS - MANAGER
       ========================================================= */

    public Page<CouncilResponse> searchCouncils(
            CouncilSearchForm form,
            Pageable pageable,
            UUID userId
    ) {
        Organization org = getManagerOrganization(userId);
        Semester semester = getCurrentSemester(org);

        Specification<Council> spec =
                CouncilSpecification.filter(form, semester.getId());

        return councilRepository.findAll(spec, pageable)
                .map(CouncilMapper::toCouncilResponse);
    }

    public List<CouncilResponse> listCouncils(
            CouncilSearchForm form,
            UUID userId
    ) {
        Organization org = getManagerOrganization(userId);
        Semester semester = getCurrentSemester(org);

        Specification<Council> spec =
                CouncilSpecification.filter(form, semester.getId());

        return councilRepository.findAll(spec)
                .stream()
                .map(CouncilMapper::toCouncilResponse)
                .toList();
    }

    /* =========================================================
                        COUNCILS - MEMBER
       ========================================================= */

    public List<CouncilResponse> getCurrentMemberCouncils(UUID userId) {

        Lecturer lecturer = getLecturer(userId);
        Semester semester = getCurrentSemesterByUser(userId);

        return getMemberCouncils(
                lecturer.getId(),
                semester.getId()
        );
    }

    public List<CouncilResponse> getMemberCouncilsBySemester(
            UUID semesterId,
            UUID userId
    ) {

        Lecturer lecturer = getLecturer(userId);

        return getMemberCouncils(
                lecturer.getId(),
                semesterId
        );
    }

    public CouncilResponse getCouncilById(UUID id, UUID userId) {

        Lecturer lecturer = getLecturer(userId);

        Council council = councilRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Council not found"));

        return CouncilMapper.toCouncilResponse(
                council,
                lecturer.getId()
        );
    }

    /* =========================================================
                        CREATE / UPDATE / DELETE
       ========================================================= */

    @Transactional
    public CouncilResponse createCouncil(
            CouncilRequest request,
            UUID userId
    ) {

        validateDuplicateLecturer(request.members());

        Organization org = getManagerOrganization(userId);
        Semester semester = getCurrentSemester(org);

        Council council = Council.builder()
                .name(request.name())
                .code(request.code())
                .semester(semester)
                .build();

        for (CouncilMemberRequest member : request.members()) {

            CouncilMember councilMember =
                    buildCouncilMember(member, council, org);

            council.getMembers().add(councilMember);
        }

        councilRepository.save(council);

        return toCouncilResponse(council);
    }

    @Transactional
    public CouncilResponse updateCouncil(
            UUID id,
            CouncilRequest request,
            UUID userId
    ) {

        validateDuplicateLecturer(request.members());

        Organization org = getManagerOrganization(userId);
        Semester semester = getCurrentSemester(org);

        Council council = councilRepository
                .findByIdAndSemester(id, semester)
                .orElseThrow(() ->
                        new BadRequestException("Council not found"));

        council.setName(request.name());
        council.setCode(request.code());

        Map<UUID, CouncilMember> currentMembers =
                council.getMembers()
                        .stream()
                        .collect(Collectors.toMap(
                                CouncilMember::getId,
                                m -> m
                        ));

        Set<CouncilMember> updatedMembers = new HashSet<>();

        for (CouncilMemberRequest memberRequest : request.members()) {

            CouncilRole role = findRole(memberRequest.roleId(), org);
            Lecturer lecturer = findLecturer(
                    memberRequest.lecturerId(),
                    org
            );

            // UPDATE
            if (memberRequest.id() != null) {

                CouncilMember member =
                        currentMembers.get(memberRequest.id());

                if (member == null) {
                    throw new BadRequestException(
                            "Invalid council member id"
                    );
                }

                member.setLecturer(lecturer);
                member.setRole(role);

                updatedMembers.add(member);

            } else {

                CouncilMember newMember =
                        CouncilMember.builder()
                                .council(council)
                                .lecturer(lecturer)
                                .role(role)
                                .build();

                updatedMembers.add(newMember);
            }
        }

        council.getMembers().clear();
        council.getMembers().addAll(updatedMembers);

        councilRepository.save(council);

        return toCouncilResponse(council);
    }

    @Transactional
    public void deleteCouncil(UUID id, UUID userId) {

        Organization org = getManagerOrganization(userId);
        Semester semester = getCurrentSemester(org);

        Council council = councilRepository
                .findByIdAndSemester(id, semester)
                .orElseThrow(() ->
                        new BadRequestException("Council not found"));

        councilRepository.delete(council);
    }

    /* =========================================================
                        COUNCIL ROLES
       ========================================================= */

    public List<CouncilRole> getCouncilRolesByUser(UUID userId) {
        return councilRoleRepository
                .findAllByOrganization(
                        orgService.findByUserId(userId)
                );
    }

    public CouncilRole addCouncilRole(
            CouncilRole request,
            UUID userId
    ) {

        Organization org = orgService.findByUserId(userId);

        CouncilRole role = CouncilRole.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .organization(org)
                .build();

        return councilRoleRepository.save(role);
    }

    public CouncilRole updateCouncilRole(
            UUID id,
            CouncilRole request,
            UUID userId
    ) {

        CouncilRole role = findCouncilRoleById(id);

        if (!role.getOrganization()
                .getManager()
                .getId()
                .equals(userId)) {

            throw new UnauthorizedException("");
        }

        role.setName(request.getName());
        role.setCode(request.getCode());
        role.setDescription(request.getDescription());

        return councilRoleRepository.save(role);
    }

    public void deleteCouncilRole(UUID id, UUID userId) {

        CouncilRole role = findCouncilRoleById(id);

        if (!role.getOrganization()
                .getManager()
                .getId()
                .equals(userId)) {

            throw new UnauthorizedException("");
        }

        councilRoleRepository.delete(role);
    }

    public CouncilRole findCouncilRoleById(UUID id) {
        return councilRoleRepository
                .findById(id)
                .orElseThrow();
    }

    /* =========================================================
                        HELPER METHODS
       ========================================================= */

    private Lecturer getLecturer(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new BadRequestException("User not found"))
                .getLecturer();
    }

    private Organization getManagerOrganization(UUID userId) {

        Organization org = orgService.findByUserId(userId);

        if (!org.getManager().getId().equals(userId)) {
            throw new UnauthorizedException(
                    "You do not have permission"
            );
        }

        return org;
    }

    private Semester getCurrentSemesterByUser(UUID userId) {

        Organization org = orgService.findByUserId(userId);

        return getCurrentSemester(org);
    }

    public Semester getCurrentSemester(Organization organization) {

        return semesterRepository
                .findByOrganizationAndStatus(
                        organization,
                        Semester.Status.ACTIVE
                )
                .orElseThrow(() ->
                        new BadRequestException(
                                "Semester not found"
                        ));
    }

    private List<CouncilResponse> getMemberCouncils(
            UUID lecturerId,
            UUID semesterId
    ) {

        List<Council> councils =
                councilMemberRepository
                        .findCouncilsByLecturerIdAndSemesterId(
                                lecturerId,
                                semesterId
                        );

        return councils.stream()
                .map(c ->
                        CouncilMapper.toCouncilResponse(
                                c,
                                lecturerId
                        ))
                .toList();
    }

    private CouncilMember buildCouncilMember(
            CouncilMemberRequest request,
            Council council,
            Organization org
    ) {

        CouncilRole role =
                findRole(request.roleId(), org);

        Lecturer lecturer =
                findLecturer(request.lecturerId(), org);

        return CouncilMember.builder()
                .council(council)
                .lecturer(lecturer)
                .role(role)
                .build();
    }

    private CouncilRole findRole(
            UUID roleId,
            Organization org
    ) {
        return councilRoleRepository
                .findByIdAndOrganization(roleId, org)
                .orElseThrow(() ->
                        new BadRequestException("Role not found"));
    }

    private Lecturer findLecturer(
            UUID lecturerId,
            Organization org
    ) {
        return lecturerRepository
                .findByIdAndOrganization(lecturerId, org)
                .orElseThrow(() ->
                        new BadRequestException("Lecturer not found"));
    }

    private void validateDuplicateLecturer(
            List<CouncilMemberRequest> members
    ) {

        Set<UUID> lecturerIds = new HashSet<>();

        for (CouncilMemberRequest member : members) {

            if (!lecturerIds.add(member.lecturerId())) {
                throw new BadRequestException(
                        "Duplicate lecturer in council"
                );
            }
        }
    }
}