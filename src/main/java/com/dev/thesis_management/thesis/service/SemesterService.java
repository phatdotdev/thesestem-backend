package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.category.entity.AcademicYear;
import com.dev.thesis_management.category.repository.AcademicYearRepository;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.specifications.MentorSpecification;
import com.dev.thesis_management.thesis.dto.*;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.mapper.SemesterMapper;
import com.dev.thesis_management.thesis.repository.SemesterRepository;
import com.dev.thesis_management.user.dto.LecturerResponse;
import com.dev.thesis_management.user.dto.MentorSearchForm;
import com.dev.thesis_management.user.dto.StudentResponse;
import com.dev.thesis_management.user.dto.StudentSearchForm;
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

import static com.dev.thesis_management.thesis.mapper.SemesterMapper.*;
import static com.dev.thesis_management.specifications.StudentSpecification.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SemesterService {

    UserRepository userRepository;
    SemesterRepository semesterRepository;
    StudentRepository studentRepository;
    LecturerRepository lecturerRepository;
    AcademicYearRepository yearRepository;

    OrgService orgService;

    /* CRUD */

    // GET SEMESTERS BY ORG
    public List<SemesterResponse> getSemesters(UUID userId){
        return semesterRepository.findByOrganization(orgService.findByUserId(userId))
                .stream().map(SemesterMapper::semesterToResponse).toList();
    }

    // GET SEMESTERS BY ORG AND YEAR
    public List<SemesterResponse> getSemesters(UUID yearId, UUID userId){
        AcademicYear year = yearRepository.findById(yearId)
                .orElseThrow(() -> new BadRequestException("Year not found"));
        return semesterRepository.findAllByOrganizationAndAcademicYear(orgService.findByUserId(userId), year)
                .stream().map(SemesterMapper::semesterToResponse).toList();
    }

    // CREATE SEMESTER
    public SemesterResponse createSemester(CreateSemesterRequest request, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);
        Semester semester = createSemesterToSemester(request);
        semester.setOrganization(org);
        semester.setStatus(Semester.Status.UPCOMING);
        return semesterToResponse(semesterRepository.save(semester));
    }

    // UPDATE SEMESTER
    public SemesterResponse updateSemester(UUID id, UpdateSemesterRequest request, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);
       Semester semester = semesterRepository.findById(id)
                       .orElseThrow(() -> new BadRequestException("Semester not found"));
        updateSemesterFromRequest(semester, request);
        return semesterToResponse(semesterRepository.save(semester));
    }

    // DELETE SEMESTER
    public void deleteSemester(UUID id, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Semester not found"));
        semesterRepository.delete(semester);
    }

    // UPDATE SEMESTER STATE
    @Transactional
    public SemesterResponse updateSemesterStatus(UUID id, Semester.Status status, UUID userId){
        Organization org = orgService.findByUserId(userId);
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

    public SemesterResponse getCurrentSemester(UUID userId){
        Organization org = orgService.findByUserId(userId);
        return semesterToResponse(getCurrentSemester(org));
    }
    /* */

    /* STUDENTS */

    // GET STUDENTS
    public List<StudentResponse> listStudentsInSemester(StudentSearchForm form, UUID userId) {
        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);

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

    // SEARCH STUDENTS
    public Page<StudentResponse> searchStudentsInSemester(
            StudentSearchForm form,
            Pageable pageable,
            UUID userId
    ) {

        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);

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

    // ADD STUDENT
    public SemesterResponse addStudent(UUID id, UUID stdId, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);

        Semester semester = semesterRepository.findByIdAndOrganization(id, org)
                .orElseThrow(() -> new BadRequestException("Semester not found"));
        Student student = studentRepository.findByIdAndOrganization(stdId, org)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        if (!semester.getStudents().contains(student)) {
            semester.getStudents().add(student);
        }
        return semesterToResponse(semesterRepository.save(semester));
    }

    public void addStudentToCurrentSemester(UUID stdId, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemester(org);
        if(semester == null) {
            throw new BadRequestException("No active semester");
        }
        Student student = studentRepository.findByIdAndOrganization(stdId, org)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        if (!semester.getStudents().contains(student)) {
            semester.getStudents().add(student);
        }
        semesterToResponse(semesterRepository.save(semester));
    }

    public void removeStudentFromCurrentSemester(UUID stdId, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);

        Semester semester = getCurrentSemester(org);
        if(semester == null) {
            throw new BadRequestException("No active semester");
        }
        Student student = studentRepository.findByIdAndOrganization(stdId, org)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        semester.getStudents().remove(student);
        semesterToResponse(semesterRepository.save(semester));
    }

    // ADD STUDENTS
    @Transactional
    public SemesterResponse addStudents(UUID id, List<UUID> stdIds, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);

        Semester semester = semesterRepository
                .findByIdAndOrganization(id, org)
                .orElseThrow(() -> new BadRequestException("Semester not found"));

        List<Student> students = studentRepository
                .findAllByIdInAndOrganization(stdIds, org);
        for (Student std: students){
            if(!semester.getStudents().contains(std)){
                semester.getStudents().add(std);
            }
        }
        semesterRepository.save(semester);
        return semesterToResponse(semester);
    }

    public Boolean checkStudentInCurrentSemester(UUID id, UUID userId) {
        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);
        return semester.getStudents().stream().map(Student::getId).toList().contains(id);
    }

    public Boolean checkStudentInCurrentSemester(UUID userId) {
        User user = getUserById(userId);
        Student student = user.getStudent();
        Organization org = student.getOrganization();
        Semester semester = getCurrentSemester(org);
        return semester.getStudents().stream().map(Student::getId).toList().contains(student.getId());
    }

    /* MENTORS */

    public List<LecturerResponse> listMentorsInSemester(MentorSearchForm form, UUID userId) {
        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);

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

    public Page<LecturerResponse> searchMentorsInSemester(MentorSearchForm form, Pageable pageable, UUID userId) {
        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);

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

        return lecturerRepository.findAll(spec, pageable)
                .map(LecturerMapper::lecturerToResponse);
    }

    // ADD MENTOR
    public SemesterResponse addMentor(UUID id, UUID mentorId, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);
        Semester semester = semesterRepository.findByIdAndOrganization(id, org)
                .orElseThrow(() -> new BadRequestException("Semester not found"));
        Lecturer mentor = lecturerRepository.findByIdAndOrganization(mentorId, org)
                .orElseThrow(() -> new BadRequestException("Mentor not found"));
        if(!semester.getMentors().contains(mentor)){
            semester.getMentors().add(mentor);
        }
        return semesterToResponse(semesterRepository.save(semester));
    }

    public void addMentorToCurrentSemester(UUID mentorId, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);
        Semester semester = getCurrentSemester(org);
        if(semester == null) {
            throw new BadRequestException("No active semester");
        }
        Lecturer mentor = lecturerRepository.findByIdAndOrganization(mentorId, org)
                .orElseThrow(() -> new BadRequestException("Mentor not found"));
        if(!semester.getMentors().contains(mentor)){
            semester.getMentors().add(mentor);
        }
        semesterToResponse(semesterRepository.save(semester));
    }

    public void removeMentorFromCurrentSemester(UUID mentorId, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);
        Semester semester = getCurrentSemester(org);
        if(semester == null) {
            throw new BadRequestException("No active semester");
        }
        Lecturer mentor = lecturerRepository.findByIdAndOrganization(mentorId, org)
                .orElseThrow(() -> new BadRequestException("Mentor not found"));
        semester.getMentors().remove(mentor);
        semesterToResponse(semesterRepository.save(semester));
    }

    // ADD MENTORS
    public SemesterResponse addMentors(UUID id, List<UUID> mentorIds, UUID userId){
        Organization org = orgService.findByUserId(userId);
        checkManager(org, userId);
        Semester semester = semesterRepository.findByIdAndOrganization(id, org)
                .orElseThrow(() -> new BadRequestException("Semester not found"));
        List<Lecturer> mentors = lecturerRepository.findAllByIdInAndOrganization(mentorIds, org);
        for(Lecturer mentor: mentors){
            if(!semester.getMentors().contains(mentor)){
                semester.getMentors().add(mentor);
            }
        }
        semesterRepository.save(semester);
        return semesterToResponse(semester);
    }

    public Boolean checkMentorInCurrentSemester(UUID id, UUID userId) {
        Organization org = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(org);
        return semester.getMentors().stream().map(Lecturer::getId).toList().contains(id);
    }

    public Boolean checkMentorInCurrentSemester(UUID userId) {
        User user = getUserById(userId);
        Lecturer lecturer = user.getLecturer();
        Organization org = lecturer.getOrganization();
        Semester semester = getCurrentSemester(org);
        return semester.getMentors().stream().map(Lecturer::getId).toList().contains(lecturer.getId());
    }

    // CURRENT SEMESTER
    public Semester getCurrentSemester(Organization organization){
        return semesterRepository.findByOrganizationAndStatus(organization, Semester.Status.ACTIVE)
                .orElse(null);
    }

    // CHECK MANAGER PERMISSION
    private void checkManager(Organization org, UUID userId) {
        if(!org.getManager().getId().equals(userId)){
            throw new UnauthorizedException("You do not have permission");
        }
    }

    // GET USER BY ID
    public User getUserById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

}
