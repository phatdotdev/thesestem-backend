package com.dev.thesis_management.statistics.service;

import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.organization.entity.College;
import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.repository.CollegeRepository;
import com.dev.thesis_management.organization.repository.DepartmentRepository;
import com.dev.thesis_management.organization.repository.FacultyRepository;
import com.dev.thesis_management.statistics.dto.SystemStatistics;
import com.dev.thesis_management.statistics.dto.semester.OrganizationStatistics;
import com.dev.thesis_management.statistics.dto.semester.OrganizationStatisticsRequest;
import com.dev.thesis_management.statistics.dto.semester.SemesterStatistic;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.entity.ThesisDefense;
import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.thesis.entity.DefenseScore;
import com.dev.thesis_management.thesis.entity.CouncilMember;
import com.dev.thesis_management.thesis.repository.GroupRepository;
import com.dev.thesis_management.thesis.repository.SemesterRepository;
import com.dev.thesis_management.thesis.repository.DefenseScoreRepository;
import com.dev.thesis_management.thesis.repository.CouncilRepository;
import com.dev.thesis_management.thesis.repository.ThesisDefenseRepository;
import com.dev.thesis_management.thesis.repository.ThesisRepository;
import com.dev.thesis_management.thesis.repository.TopicRepository;
import com.dev.thesis_management.user.repository.LecturerRepository;
import com.dev.thesis_management.user.repository.StudentRepository;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.enums.UserRole;
import com.dev.thesis_management.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticsService {

    UserRepository userRepository;
    StudentRepository studentRepository;
    LecturerRepository lecturerRepository;
    CollegeRepository collegeRepository;
    FacultyRepository facultyRepository;
    DepartmentRepository departmentRepository;
    ThesisRepository thesisRepository;
    SemesterRepository semesterRepository;
    CouncilRepository councilRepository;
    GroupRepository groupRepository;
    TopicRepository topicRepository;
    ThesisDefenseRepository thesisDefenseRepository;
    DefenseScoreRepository defenseScoreRepository;
    StringRedisTemplate redisTemplate;

    private static final String SYSTEM_STATS_KEY = "system:statistics";

    public SystemStatistics getSystemStatistics(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only admin can access system statistics");
        }

        // 1. check redis
        String cached = redisTemplate.opsForValue().get(SYSTEM_STATS_KEY);

        if (cached != null) {
            try {
                return new ObjectMapper().readValue(cached, SystemStatistics.class);
            } catch (Exception e) {
                redisTemplate.delete(SYSTEM_STATS_KEY);
            }
        }

        // 2. query DB
        SystemStatistics statistics = SystemStatistics.builder()
                .totalUsers(userRepository.count())
                .totalManagers(userRepository.countByRole(UserRole.MANAGER))
                .totalStudents(userRepository.countByRole(UserRole.STUDENT))
                .totalLecturers(userRepository.countByRole(UserRole.LECTURER))
                .totalTheses(thesisRepository.count())
                .totalPublishedTheses(
                        thesisRepository.countByAccessLevel(Thesis.AccessLevel.PUBLIC))
                .totalInternalTheses(
                        thesisRepository.countByAccessLevel(Thesis.AccessLevel.INTERNAL))
                .totalPrivateTheses(
                        thesisRepository.countByAccessLevel(Thesis.AccessLevel.PRIVATE))
                .build();

        // 3. save redis (5 phút)
        try {
            ObjectMapper mapper = new ObjectMapper();

            redisTemplate.opsForValue().set(
                    SYSTEM_STATS_KEY,
                    mapper.writeValueAsString(statistics),
                    Duration.ofMinutes(5)
            );
        } catch (Exception ignored) {}

        return statistics;
    }

    @Transactional(readOnly = true)
    public SemesterStatistic getSemesterStatistics(UUID semesterId, UUID userId) {
        if (userId == null) {
            throw new UnauthorizedException("User ID is null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new UnauthorizedException("Semester not found"));

//        validateSemesterAccess(user, semester);

        SemesterStatistic.SemesterDetail detail = SemesterStatistic.SemesterDetail.builder()
                .id(semester.getId())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .status(semester.getStatus().name())
                .organizationId(semester.getOrganization().getId())
                .organizationName(semester.getOrganization().getName())
                .academicYearId(semester.getAcademicYear().getId())
                .academicYearName(semester.getAcademicYear().getName())
                .createdAt(semester.getCreatedAt())
                .updatedAt(semester.getUpdatedAt())
                .build();

        SemesterStatistic.SemesterMetrics metrics = SemesterStatistic.SemesterMetrics.builder()
                .totalStudents(semester.getStudents().size())
                .totalMentors(semester.getMentors().size())
                .totalGroups(groupRepository.countBySemester_Id(semesterId))
                .totalTopics(topicRepository.countByGroup_Semester_Id(semesterId))
                .totalTheses(thesisRepository.countByTopic_Group_Semester_Id(semesterId))
                .proposalTheses(thesisRepository.countByTopic_Group_Semester_IdAndStatus(semesterId, Thesis.Status.PROPOSAL))
                .inProgressTheses(thesisRepository.countByTopic_Group_Semester_IdAndStatus(semesterId, Thesis.Status.IN_PROGRESS))
                .submittedTheses(thesisRepository.countByTopic_Group_Semester_IdAndStatus(semesterId, Thesis.Status.SUBMITTED))
                .gradedTheses(thesisRepository.countByTopic_Group_Semester_IdAndStatus(semesterId, Thesis.Status.GRADED))
                .totalDefenses(thesisDefenseRepository.countByThesis_Topic_Group_Semester_Id(semesterId))
                .scheduledDefenses(thesisDefenseRepository.countByThesis_Topic_Group_Semester_IdAndStatus(semesterId, ThesisDefense.Status.SCHEDULED))
                .completedDefenses(thesisDefenseRepository.countByThesis_Topic_Group_Semester_IdAndStatus(semesterId, ThesisDefense.Status.COMPLETED))
                .cancelledDefenses(thesisDefenseRepository.countByThesis_Topic_Group_Semester_IdAndStatus(semesterId, ThesisDefense.Status.CANCELLED))
                .build();

        List<Thesis> theses = thesisRepository.findDetailedBySemesterId(semesterId);
        Map<UUID, ThesisDefense> defensesByThesisId = thesisDefenseRepository.findDetailedBySemesterId(semesterId)
                .stream()
                .collect(Collectors.toMap(
                        defense -> defense.getThesis().getId(),
                        Function.identity(),
                        (left, ignored) -> left
                ));

        List<SemesterStatistic.ThesisInsight> thesisInsights = theses.stream()
                .map(thesis -> toThesisInsight(thesis, defensesByThesisId.get(thesis.getId())))
                .toList();

        return SemesterStatistic.builder()
                .semester(detail)
                .metrics(metrics)
                .theses(thesisInsights)
                .build();
    }

    private SemesterStatistic.ThesisInsight toThesisInsight(Thesis thesis, ThesisDefense defense) {
        List<Double> scoredValues = defense == null
                ? List.of()
                : defense.getScores().stream()
                .map(DefenseScore::getScore)
                .filter(java.util.Objects::nonNull)
                .toList();

        Double averageScore = scoredValues.isEmpty()
                ? null
                : scoredValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0d);

        return SemesterStatistic.ThesisInsight.builder()
                .thesisId(thesis.getId())
                .title(thesis.getTitle())
                .titleEn(thesis.getTitleEn())
                .description(thesis.getDescription())
                .descriptionEn(thesis.getDescriptionEn())
                .status(thesis.getStatus() != null ? thesis.getStatus().name() : null)
                .accessLevel(thesis.getAccessLevel() != null ? thesis.getAccessLevel().name() : null)
                .progressPercent(thesis.getProgressPercent())
                .createdAt(thesis.getCreatedAt())
                .topic(SemesterStatistic.TopicBrief.builder()
                        .id(thesis.getTopic().getId())
                        .title(thesis.getTopic().getTitle())
                        .description(thesis.getTopic().getDescription())
                        .maxStudents(thesis.getTopic().getMaxStudents())
                        .build())
                .group(SemesterStatistic.GroupBrief.builder()
                        .id(thesis.getTopic().getGroup().getId())
                        .name(thesis.getTopic().getGroup().getName())
                        .description(thesis.getTopic().getGroup().getDescription())
                        .build())
                .student(SemesterStatistic.StudentBrief.builder()
                        .id(thesis.getStudent().getId())
                        .code(thesis.getStudent().getStudentCode())
                        .fullName(thesis.getStudent().getFullName())
                        .email(thesis.getStudent().getEmail())
                        .build())
                .mentor(thesis.getTopic().getGroup().getMentor() == null
                        ? null
                        : SemesterStatistic.MentorBrief.builder()
                        .id(thesis.getTopic().getGroup().getMentor().getId())
                        .code(thesis.getTopic().getGroup().getMentor().getLecturerCode())
                        .fullName(thesis.getTopic().getGroup().getMentor().getFullName())
                        .email(thesis.getTopic().getGroup().getMentor().getEmail())
                        .build())
                .defense(toDefenseBrief(defense))
                .averageScore(averageScore)
                .build();
    }

    private SemesterStatistic.DefenseBrief toDefenseBrief(ThesisDefense defense) {
        if (defense == null) {
            return null;
        }

        List<SemesterStatistic.CouncilMemberBrief> members = defense.getCouncil() == null
                ? List.of()
                : defense.getCouncil().getMembers().stream()
                .sorted(Comparator.comparing(CouncilMember::getId))
                .map(member -> SemesterStatistic.CouncilMemberBrief.builder()
                        .id(member.getId())
                        .lecturerId(member.getLecturer().getId())
                        .lecturerCode(member.getLecturer().getLecturerCode())
                        .lecturerName(member.getLecturer().getFullName())
                        .roleCode(member.getRole().getCode())
                        .roleName(member.getRole().getName())
                        .build())
                .toList();

        List<SemesterStatistic.ScoreBrief> scores = defense.getScores().stream()
                .sorted(Comparator.comparing(DefenseScore::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(score -> SemesterStatistic.ScoreBrief.builder()
                        .id(score.getId())
                        .councilMemberId(score.getCouncilMember().getId())
                        .lecturerId(score.getCouncilMember().getLecturer().getId())
                        .lecturerCode(score.getCouncilMember().getLecturer().getLecturerCode())
                        .lecturerName(score.getCouncilMember().getLecturer().getFullName())
                        .roleCode(score.getCouncilMember().getRole().getCode())
                        .roleName(score.getCouncilMember().getRole().getName())
                        .score(score.getScore())
                        .comment(score.getComment())
                        .createdAt(score.getCreatedAt())
                        .build())
                .toList();

        return SemesterStatistic.DefenseBrief.builder()
                .id(defense.getId())
                .status(defense.getStatus() != null ? defense.getStatus().name() : null)
                .defenseTime(defense.getDefenseTime())
                .location(defense.getLocation())
                .council(defense.getCouncil() == null
                        ? null
                        : SemesterStatistic.CouncilBrief.builder()
                        .id(defense.getCouncil().getId())
                        .code(defense.getCouncil().getCode())
                        .name(defense.getCouncil().getName())
                        .members(members)
                        .build())
                .scores(scores)
                .build();
    }

    private void validateSemesterAccess(User user, Semester semester) {
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        if (user.getRole() == UserRole.MANAGER
                && user.getOrganization() != null
                && user.getOrganization().getId().equals(semester.getOrganization().getId())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to access this semester statistics");
    }

    @Transactional(readOnly = true)
    public OrganizationStatistics getOrganizationStatistics(OrganizationStatisticsRequest request, UUID userId) {
        if (request == null) {
            throw new BadRequestException("Request is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        Organization organization = resolveOrganization(user);
        validateScopeHierarchy(request, organization);
        validateSemesterBelongsToOrganization(request.semesterId(), organization.getId());

        long totalStudents = studentRepository.countByScope(
                organization.getId(),
                null,
                request.collegeId(),
                request.facultyId(),
                request.departmentId()
        );

        long totalLecturers = lecturerRepository.countByScope(
                organization.getId(),
                null,
                request.collegeId(),
                request.facultyId(),
                request.departmentId()
        );

        long totalTheses = thesisRepository.countByScope(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId()
        );

        long proposalTheses = thesisRepository.countByScopeAndStatus(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.Status.PROPOSAL
        );

        long inProgressTheses = thesisRepository.countByScopeAndStatus(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.Status.IN_PROGRESS
        );

        long approvedTheses = thesisRepository.countByScopeAndStatus(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.Status.APPROVED
        );

        long submittedTheses = thesisRepository.countByScopeAndStatus(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.Status.SUBMITTED
        );

        long gradedTheses = thesisRepository.countByScopeAndStatus(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.Status.GRADED
        );

        long privateTheses = thesisRepository.countByScopeAndAccessLevel(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.AccessLevel.PRIVATE
        );

        long internalTheses = thesisRepository.countByScopeAndAccessLevel(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.AccessLevel.INTERNAL
        );

        long publicTheses = thesisRepository.countByScopeAndAccessLevel(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId(),
                Thesis.AccessLevel.PUBLIC
        );

        long totalDefenses = thesisDefenseRepository.countByScope(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId()
        );

        Double averageThesisScore = defenseScoreRepository.findAverageThesisScoreByScope(
                organization.getId(),
                request.semesterId(),
                request.collegeId(),
                request.facultyId(),
                request.departmentId()
        );

        long totalCouncils = councilRepository.countCouncils(
                organization.getId(),
                request.semesterId(),
                request.departmentId(),
                request.facultyId(),
                request.collegeId()
        );

        OrganizationStatistics.OrganizationStatisticsBuilder builder = OrganizationStatistics.builder()
                .totalStudents(totalStudents)
                .totalLecturers(totalLecturers)
                .totalTheses(totalTheses)
                .proposalTheses(proposalTheses)
                .inProgressTheses(inProgressTheses)
                .approvedTheses(approvedTheses)
                .submittedTheses(submittedTheses)
                .gradedTheses(gradedTheses)
                .privateTheses(privateTheses)
                .internalTheses(internalTheses)
                .publicTheses(publicTheses)
                .totalCouncils(totalCouncils)
                .totalDefenses(totalDefenses)
                .averageThesisScore(averageThesisScore != null ? averageThesisScore : 0.0d);

        if (request.semesterId() != null) {
            long totalGroups = groupRepository.countBySemesterAndMentorScope(
                    request.semesterId(),
                    organization.getId(),
                    request.collegeId(),
                    request.facultyId(),
                    request.departmentId()
            );

            long totalMentors = groupRepository.countDistinctMentorBySemesterAndScope(
                    request.semesterId(),
                    organization.getId(),
                    request.collegeId(),
                    request.facultyId(),
                    request.departmentId()
            );

            builder.totalGroups(totalGroups)
                    .totalMentors(totalMentors);
        }

        return builder.build();
    }

    private Organization resolveOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new BadRequestException("Organization not found for current user");
        }

        return user.getOrganization();
    }

    private void validateSemesterBelongsToOrganization(UUID semesterId, UUID organizationId) {
        if (semesterId == null) {
            return;
        }

        semesterRepository.findById(semesterId)
                .filter(semester -> semester.getOrganization() != null
                        && semester.getOrganization().getId().equals(organizationId))
                .orElseThrow(() -> new BadRequestException("Semester does not belong to this organization"));
    }

    private void validateScopeHierarchy(OrganizationStatisticsRequest request, Organization organization) {
        College college = null;
        Faculty faculty = null;

        if (request.collegeId() != null) {
            college = collegeRepository.findByIdAndOrganization(request.collegeId(), organization)
                    .orElseThrow(() -> new BadRequestException("College not found in organization"));
        }

        if (request.facultyId() != null) {
            faculty = facultyRepository.findByIdAndOrganization(request.facultyId(), organization)
                    .orElseThrow(() -> new BadRequestException("Faculty not found in organization"));

            if (college != null && (faculty.getCollege() == null || !faculty.getCollege().getId().equals(college.getId()))) {
                throw new BadRequestException("Faculty does not belong to selected college");
            }
        }

        if (request.departmentId() != null) {
            Department department = departmentRepository.findByIdAndOrganization(request.departmentId(), organization)
                    .orElseThrow(() -> new BadRequestException("Department not found in organization"));

            if (faculty != null && (department.getFaculty() == null || !department.getFaculty().getId().equals(faculty.getId()))) {
                throw new BadRequestException("Department does not belong to selected faculty");
            }

            if (college != null
                    && (department.getFaculty() == null
                    || department.getFaculty().getCollege() == null
                    || !department.getFaculty().getCollege().getId().equals(college.getId()))) {
                throw new BadRequestException("Department does not belong to selected college");
            }
        }
    }

    private boolean hasUnitScope(OrganizationStatisticsRequest request) {
        return request.collegeId() != null
                || request.facultyId() != null
                || request.departmentId() != null;
    }
}
