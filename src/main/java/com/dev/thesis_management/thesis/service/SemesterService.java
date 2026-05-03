package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.category.repository.AcademicYearRepository;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.specifications.MentorSpecification;
import com.dev.thesis_management.thesis.dto.*;
import com.dev.thesis_management.thesis.dto.milestone.MilestoneRequest;
import com.dev.thesis_management.thesis.dto.milestone.MilestoneResponse;
import com.dev.thesis_management.thesis.entity.Milestone;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.mapper.MilestoneMapper;
import com.dev.thesis_management.thesis.mapper.SemesterMapper;
import com.dev.thesis_management.thesis.repository.MilestoneRepository;
import com.dev.thesis_management.thesis.repository.SemesterRepository;
import com.dev.thesis_management.user.dto.*;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.mapper.LecturerMapper;
import com.dev.thesis_management.user.mapper.StudentMapper;
import com.dev.thesis_management.user.repository.LecturerRepository;
import com.dev.thesis_management.user.repository.StudentRepository;
import com.dev.thesis_management.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.dev.thesis_management.specifications.StudentSpecification.search;
import static com.dev.thesis_management.thesis.mapper.SemesterMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SemesterService {

    UserRepository userRepository;
    SemesterRepository semesterRepository;
    StudentRepository studentRepository;
    LecturerRepository lecturerRepository;
    AcademicYearRepository yearRepository;
    MilestoneRepository milestoneRepository;
    OrgService orgService;

    /* =========================
            SEMESTER CRUD
       ========================= */

    public List<SemesterResponse> getSemesters(UUID userId) {
        return semesterRepository
                .findByOrganizationOrderByAcademicYearNameDescNameDesc(getOrg(userId))
                .stream()
                .map(SemesterMapper::semesterToResponse)
                .toList();
    }

    public List<SemesterResponse> getSemesters(UUID yearId, UUID userId) {
        AcademicYear year = yearRepository.findById(yearId)
                .orElseThrow(() -> new BadRequestException("Year not found"));

        return semesterRepository
                .findAllByOrganizationAndAcademicYearOrderByNameDesc(getOrg(userId), year)
                .stream()
                .map(SemesterMapper::semesterToResponse)
                .toList();
    }

    public SemesterResponse createSemester(CreateSemesterRequest request, UUID userId) {
        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = createSemesterToSemester(request);
        semester.setOrganization(org);
        semester.setStatus(Semester.Status.UPCOMING);

        return semesterToResponse(semesterRepository.save(semester));
    }

    public SemesterResponse updateSemester(UUID id, UpdateSemesterRequest request, UUID userId) {
        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Semester not found"));

        updateSemesterFromRequest(semester, request);

        return semesterToResponse(semesterRepository.save(semester));
    }

    public void deleteSemester(UUID id, UUID userId) {
        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Semester not found"));

        semesterRepository.delete(semester);
    }

    @Transactional
    public SemesterResponse updateSemesterStatus(UUID id, Semester.Status status, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Semester not found"));

        if (status == Semester.Status.ACTIVE) {

            Semester current = semesterRepository
                    .findByOrganizationAndStatus(org, Semester.Status.ACTIVE)
                    .orElse(null);

            if (current != null && !current.getId().equals(id)) {
                current.setStatus(Semester.Status.FINISHED);
                semesterRepository.save(current);
            }
        }

        semester.setStatus(status);
        semesterRepository.save(semester);

        return semesterToResponse(semester);
    }

    public SemesterResponse getCurrentSemester(UUID userId) {
        return semesterToResponse(getCurrentSemester(getOrg(userId)));
    }

    /* =========================
            STUDENTS
       ========================= */

    public List<StudentResponse> listStudentsBySemester(StudentSearchForm form, UUID semesterId, UUID userId) {
        Organization org = getOrg(userId);
        Semester semester = getSemester(semesterId, org);

        Specification<Student> spec = search(form, org.getId());

        spec = spec.and((root, query, cb) ->
                root.get("id").in(
                        semester.getStudents()
                                .stream()
                                .map(Student::getId)
                                .toList()
                )
        );

        return studentRepository.findAll(spec)
                .stream()
                .map(StudentMapper::studentToResponse)
                .toList();
    }


    public List<StudentResponse> listStudentsInSemester(StudentSearchForm form, UUID userId) {

        Organization org = getOrg(userId);
        Semester semester = getCurrentSemesterOrThrow(org);

        Specification<Student> spec = search(form, org.getId());

        spec = spec.and((root, query, cb) ->
                root.get("id").in(
                        semester.getStudents()
                                .stream()
                                .map(Student::getId)
                                .toList()
                )
        );

        return studentRepository.findAll(spec)
                .stream()
                .map(StudentMapper::studentToResponse)
                .toList();
    }

    public Page<StudentResponse> searchStudentsInSemester(
            StudentSearchForm form,
            Pageable pageable,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        Semester semester = getCurrentSemesterOrThrow(org);

        Specification<Student> spec = search(form, org.getId());

        spec = spec.and((root, query, cb) ->
                root.get("id").in(
                        semester.getStudents()
                                .stream()
                                .map(Student::getId)
                                .toList()
                )
        );

        return studentRepository.findAll(spec, pageable)
                .map(StudentMapper::studentToResponse);
    }

    public Page<StudentResponse> searchStudentsBySemester(
            StudentSearchForm form,
            UUID semesterId,
            Pageable pageable,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        Semester semester = getSemester(semesterId, org);

        Specification<Student> spec = search(form, org.getId());

        spec = spec.and((root, query, cb) ->
                root.get("id").in(
                        semester.getStudents()
                                .stream()
                                .map(Student::getId)
                                .toList()
                )
        );

        return studentRepository.findAll(spec, pageable)
                .map(StudentMapper::studentToResponse);
    }

    public SemesterResponse addStudent(UUID id, UUID stdId, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getSemester(id, org);

        Student student = studentRepository
                .findByIdAndOrganization(stdId, org)
                .orElseThrow(() -> new BadRequestException("Student not found"));

        if (!semester.getStudents().contains(student)) {
            semester.getStudents().add(student);
        }

        return semesterToResponse(semesterRepository.save(semester));
    }

    public void addStudentToCurrentSemester(UUID stdId, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemesterOrThrow(org);

        Student student = studentRepository
                .findByIdAndOrganization(stdId, org)
                .orElseThrow(() -> new BadRequestException("Student not found"));

        if (!semester.getStudents().contains(student)) {
            semester.getStudents().add(student);
        }

        semesterRepository.save(semester);
    }

    public void removeStudentFromCurrentSemester(UUID stdId, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemesterOrThrow(org);

        Student student = studentRepository
                .findByIdAndOrganization(stdId, org)
                .orElseThrow(() -> new BadRequestException("Student not found"));

        semester.getStudents().remove(student);
        semesterRepository.save(semester);
    }

    @Transactional
    public SemesterResponse addStudents(UUID id, List<UUID> stdIds, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getSemester(id, org);

        List<Student> students =
                studentRepository.findAllByIdInAndOrganization(stdIds, org);

        for (Student std : students) {
            if (!semester.getStudents().contains(std)) {
                semester.getStudents().add(std);
            }
        }

        semesterRepository.save(semester);

        return semesterToResponse(semester);
    }

    public Boolean checkStudentInSemester(UUID id, UUID userId) {

        Organization org = getOrg(userId);
        Semester semester = getCurrentSemesterOrThrow(org);

        return semester.getStudents()
                .stream()
                .map(Student::getId)
                .toList()
                .contains(id);
    }

    public Boolean checkStudentInCurrentSemester(UUID userId) {

        User user = getUserById(userId);
        Student student = user.getStudent();

        Organization org = student.getOrganization();
        Semester semester = getCurrentSemesterOrThrow(org);

        return semester.getStudents()
                .stream()
                .map(Student::getId)
                .toList()
                .contains(student.getId());
    }

    /* =========================
            MENTORS
       ========================= */

    public List<LecturerResponse> listMentorsInSemester(
            MentorSearchForm form,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        Semester semester = getCurrentSemesterOrThrow(org);

        Specification<Lecturer> spec =
                MentorSpecification.search(form, org.getId());

        spec = spec.and((root, query, cb) ->
                root.get("id").in(
                        semester.getMentors()
                                .stream()
                                .map(Lecturer::getId)
                                .toList()
                )
        );

        return lecturerRepository.findAll(spec)
                .stream()
                .map(LecturerMapper::lecturerToResponse)
                .toList();
    }

    public Page<LecturerResponse> searchMentorsInSemester(
            MentorSearchForm form,
            Pageable pageable,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        Semester semester = getCurrentSemesterOrThrow(org);

        Specification<Lecturer> spec =
                MentorSpecification.search(form, org.getId());

        spec = spec.and((root, query, cb) ->
                root.get("id").in(
                        semester.getMentors()
                                .stream()
                                .map(Lecturer::getId)
                                .toList()
                )
        );

        return lecturerRepository
                .findAll(spec, pageable)
                .map(LecturerMapper::lecturerToResponse);
    }

    public Page<LecturerResponse> searchMentorsBySemester(
            MentorSearchForm form,
            UUID id,
            Pageable pageable,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        Semester semester = getSemester(id, org);

        Specification<Lecturer> spec =
                MentorSpecification.search(form, org.getId());

        spec = spec.and((root, query, cb) ->
                root.get("id").in(
                        semester.getMentors()
                                .stream()
                                .map(Lecturer::getId)
                                .toList()
                )
        );

        return lecturerRepository
                .findAll(spec, pageable)
                .map(LecturerMapper::lecturerToResponse);
    }

    public SemesterResponse addMentor(UUID id, UUID mentorId, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getSemester(id, org);

        Lecturer mentor = lecturerRepository
                .findByIdAndOrganization(mentorId, org)
                .orElseThrow(() -> new BadRequestException("Mentor not found"));

        if (!semester.getMentors().contains(mentor)) {
            semester.getMentors().add(mentor);
        }

        return semesterToResponse(semesterRepository.save(semester));
    }

    public void addMentorToCurrentSemester(UUID mentorId, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemesterOrThrow(org);

        Lecturer mentor = lecturerRepository
                .findByIdAndOrganization(mentorId, org)
                .orElseThrow(() -> new BadRequestException("Mentor not found"));

        if (!semester.getMentors().contains(mentor)) {
            semester.getMentors().add(mentor);
        }

        semesterRepository.save(semester);
    }

    public void removeMentorFromCurrentSemester(UUID mentorId, UUID userId) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemesterOrThrow(org);

        Lecturer mentor = lecturerRepository
                .findByIdAndOrganization(mentorId, org)
                .orElseThrow(() -> new BadRequestException("Mentor not found"));

        semester.getMentors().remove(mentor);
        semesterRepository.save(semester);
    }

    public SemesterResponse addMentors(
            UUID id,
            List<UUID> mentorIds,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getSemester(id, org);

        List<Lecturer> mentors =
                lecturerRepository.findAllByIdInAndOrganization(mentorIds, org);

        for (Lecturer mentor : mentors) {
            if (!semester.getMentors().contains(mentor)) {
                semester.getMentors().add(mentor);
            }
        }

        semesterRepository.save(semester);

        return semesterToResponse(semester);
    }

    public Boolean checkMentorInCurrentSemester(UUID id, UUID userId) {

        Organization org = getOrg(userId);
        Semester semester = getCurrentSemesterOrThrow(org);

        return semester.getMentors()
                .stream()
                .map(Lecturer::getId)
                .toList()
                .contains(id);
    }

    public Boolean checkMentorInCurrentSemester(UUID userId) {

        User user = getUserById(userId);
        Lecturer lecturer = user.getLecturer();

        Organization org = lecturer.getOrganization();
        Semester semester = getCurrentSemesterOrThrow(org);

        return semester.getMentors()
                .stream()
                .map(Lecturer::getId)
                .toList()
                .contains(lecturer.getId());
    }

    /* =========================
            MILESTONES
       ========================= */

    public MilestoneResponse createMilestone(
            MilestoneRequest request,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemester(org);

        Milestone milestone = Milestone.builder()
                .title(request.title())
                .description(request.description())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .semester(semester)
                .build();

        milestoneRepository.save(milestone);

        return MilestoneMapper.toResponse(milestone);
    }

    public MilestoneResponse updateMilestone(
            UUID milestoneId,
            MilestoneRequest request,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemester(org);
        Milestone milestone = getMilestone(milestoneId, semester);

        milestone.setTitle(request.title());
        milestone.setDescription(request.description());
        milestone.setStartAt(request.startAt());
        milestone.setEndAt(request.endAt());

        milestoneRepository.save(milestone);

        return MilestoneMapper.toResponse(milestone);
    }

    public void deleteMilestone(
            UUID semesterId,
            UUID milestoneId,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        checkManager(org, userId);

        Semester semester = getSemester(semesterId, org);
        Milestone milestone = getMilestone(milestoneId, semester);

        milestoneRepository.delete(milestone);
    }

    public List<MilestoneResponse> getMilestones(
            UUID semesterId,
            UUID userId
    ) {

        Organization org = getOrg(userId);
        Semester semester = getSemester(semesterId, org);

        return milestoneRepository
                .findBySemester(semester)
                .stream()
                .map(MilestoneMapper::toResponse)
                .toList();
    }

    public List<MilestoneResponse> getCurrentMilestones(UUID userId) {

        Organization org = getOrg(userId);
        Semester semester = getCurrentSemesterOrThrow(org);

        return milestoneRepository
                .findBySemester(semester)
                .stream()
                .map(MilestoneMapper::toResponse)
                .toList();
    }

    /* =========================
            HELPERS
       ========================= */

    public Semester getCurrentSemester(Organization organization) {
        return semesterRepository
                .findByOrganizationAndStatus(
                        organization,
                        Semester.Status.ACTIVE
                )
                .orElse(null);
    }

    private Organization getOrg(UUID userId) {
        return orgService.findByUserId(userId);
    }

    private Semester getSemester(UUID semesterId, Organization org) {
        return semesterRepository
                .findByIdAndOrganization(semesterId, org)
                .orElseThrow(() -> new BadRequestException("Semester not found"));
    }

    private Semester getCurrentSemesterOrThrow(Organization org) {
        Semester semester = getCurrentSemester(org);

        if (semester == null) {
            throw new BadRequestException("No active semester");
        }

        return semester;
    }

    private Milestone getMilestone(UUID milestoneId, Semester semester) {
        return milestoneRepository
                .findByIdAndSemester(milestoneId, semester)
                .orElseThrow(() -> new BadRequestException("Milestone not found"));
    }

    private void checkManager(Organization org, UUID userId) {
        if (!org.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}