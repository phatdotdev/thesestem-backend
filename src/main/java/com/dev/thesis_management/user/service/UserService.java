package com.dev.thesis_management.user.service;

import com.dev.thesis_management.auth.dto.RegisterRequest;
import com.dev.thesis_management.category.entity.Course;
import com.dev.thesis_management.category.repository.CourseRepository;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.exception.ConflictException;
import com.dev.thesis_management.exception.UnauthorizedException;
import com.dev.thesis_management.file_asset.entity.FileAsset;
import com.dev.thesis_management.file_asset.service.FileService;
import com.dev.thesis_management.infra.storage.StorageService;
import com.dev.thesis_management.organization.dto.ManagerSearchForm;
import com.dev.thesis_management.organization.entity.*;
import com.dev.thesis_management.organization.repository.CollegeRepository;
import com.dev.thesis_management.organization.repository.DepartmentRepository;
import com.dev.thesis_management.organization.repository.FacultyRepository;
import com.dev.thesis_management.organization.repository.ProgramRepository;
import com.dev.thesis_management.specifications.ManagerSpecification;
import com.dev.thesis_management.specifications.MentorSpecification;
import com.dev.thesis_management.specifications.StudentSpecification;
import com.dev.thesis_management.user.dto.MentorSearchForm;
import com.dev.thesis_management.user.dto.StudentSearchForm;
import com.dev.thesis_management.user.dto.*;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.enums.UserRole;
import com.dev.thesis_management.user.mapper.LecturerMapper;
import com.dev.thesis_management.user.mapper.StudentMapper;
import com.dev.thesis_management.user.mapper.UserMapper;
import com.dev.thesis_management.user.repository.LecturerRepository;
import com.dev.thesis_management.user.repository.OrganizationRepository;
import com.dev.thesis_management.user.repository.StudentRepository;
import com.dev.thesis_management.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dev.thesis_management.user.mapper.StudentMapper.*;
import static com.dev.thesis_management.user.mapper.LecturerMapper.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    OrganizationRepository organizationRepository;
    StudentRepository studentRepository;
    LecturerRepository lecturerRepository;

    FileService fileAssetService;
    StorageService storageService;

    PasswordEncoder passwordEncoder;

    ProgramRepository programRepository;
    CollegeRepository collegeRepository;
    FacultyRepository facultyRepository;
    DepartmentRepository departmentRepository;
    CourseRepository courseRepository;


    public User getUserById(UUID userId){
        return userRepository.findById(userId).orElseThrow();
    }

    public UserAccountResponse getCurrentUserAccount(UUID userId) {
        User user = getUserById(userId);
        Organization organization = user.getOrganization() != null ? user.getOrganization()
                : user.getStudent() != null ? user.getStudent().getOrganization()
                : user.getLecturer() != null ? user.getLecturer().getOrganization()
                : null;
        assert organization != null;
        return UserAccountResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .organizationId(organization.getId())
                .organizationCode(organization.getCode())
                .build();
    }

    /* USERS */
    public User register(RegisterRequest request){
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.MANAGER)
                .enabled(true)
                .build();

        userRepository.save(user);

        if (user.getRole() == UserRole.MANAGER) {
            createOrganizationForManager(user);
        }

        return user;
    }

    /* STUDENTS */
    public List<StudentResponse> getStudents(UUID userId){
        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        return studentRepository.findAllByOrganization(organization)
                .stream().map(StudentMapper::studentToResponse).toList();
    }

    public Page<StudentResponse> searchStudents(StudentSearchForm form, Pageable pageable, UUID userId) {
        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Specification<Student> spec = StudentSpecification.search(form, organization.getId());

        return studentRepository.findAll(spec, pageable)
                .map(StudentMapper::studentToResponse);
    }

    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request, UUID userId) {

        Organization organization = getUserById(userId).getOrganization();
        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Program program = programRepository
                .findByIdAndOrganization(request.programId(), organization)
                .orElseThrow(() -> new BadRequestException("Program not found"));

        Course course = courseRepository.findByIdAndOrganization(request.courseId(), organization)
                .orElseThrow(() -> new BadRequestException("Course not found"));

        Student student = createStudentRequestToStudent(request);
        student.setOrganization(organization);
        student.setProgram(program);
        student.setCourse(course);

        User user = User.builder()
                .username(request.studentCode())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.STUDENT)
                .enabled(true)
                .build();

        student.setUser(user);
        user.setStudent(student);

        userRepository.save(user);

        return studentToResponse(student);
    }

    @Transactional
    public List<StudentResponse> createStudents(
            List<CreateStudentRequest> requests,
            UUID userId
    ) {

        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        List<StudentResponse> responses = new ArrayList<>();

        for (CreateStudentRequest request : requests) {

            Program program = programRepository
                    .findByIdAndOrganization(request.programId(), organization)
                    .orElseThrow(() ->
                            new BadRequestException(
                                    "Program not found: " + request.programId()
                            )
                    );

            Course course = courseRepository
                    .findByIdAndOrganization(request.courseId(), organization)
                    .orElseThrow(() ->
                            new BadRequestException(
                                    "Course not found: " + request.courseId()
                            )
                    );

            Student student = createStudentRequestToStudent(request);

            student.setOrganization(organization);
            student.setProgram(program);
            student.setCourse(course);

            User user = User.builder()
                    .username(request.studentCode())
                    .password(passwordEncoder.encode(request.password()))
                    .role(UserRole.STUDENT)
                    .enabled(true)
                    .build();

            student.setUser(user);
            user.setStudent(student);

            userRepository.save(user);

            responses.add(studentToResponse(student));
        }

        return responses;
    }

    @Transactional
    public StudentResponse updateStudent(UUID studentId, UpdateStudentRequest request , UUID userId){

        Organization organization = getUserById(userId).getOrganization();
        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Student student = studentRepository
                .findByIdAndOrganization(studentId, organization)
                .orElseThrow(() -> new BadRequestException("Student not found"));

        if (request.fullName() != null) {
            student.setFullName(request.fullName());
        }

        if (request.email() != null) {
            student.setEmail(request.email());
        }

        if (request.programId() != null) {
            Program program = programRepository
                    .findByIdAndOrganization(request.programId(), organization)
                    .orElseThrow(() -> new BadRequestException("Program not found"));
            student.setProgram(program);
        }

        if (request.courseId() != null) {
            Course course = courseRepository
                    .findByIdAndOrganization(request.courseId(), organization)
                    .orElseThrow(() -> new BadRequestException("Course not found"));
            student.setCourse(course);
        }

        if(request.studentCode() != null && !request.studentCode().isBlank()){
            User user = student.getUser();
            user.setUsername(request.studentCode());
            student.setStudentCode(request.studentCode());
        }

        if (request.password() != null && !request.password().isBlank()) {
            User user = student.getUser();
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if(request.gender() != null && !request.gender().isBlank()){
            student.setGender(request.gender());
        }

        if(request.phone() != null && !request.phone().isBlank()){
            student.setPhone(request.phone());
        }

        studentRepository.save(student);

        return studentToResponse(student);
    }

    @Transactional
    public void deleteStudent(UUID studentId, UUID userId) {

        Organization organization = getUserById(userId).getOrganization();
        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Student student = studentRepository
                .findByIdAndOrganization(studentId, organization)
                .orElseThrow(() -> new BadRequestException("Student not found"));

        User user = student.getUser();

        student.setUser(null);
        user.setStudent(null);

        studentRepository.delete(student);
        userRepository.delete(user);
    }

    public StudentResponse getStudentProfile(UUID userId) {
        StudentResponse response = studentToResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new BadRequestException("Lecturer not found"))
                        .getStudent()
        );
        enrichStudentResponse(response);
        return response;
    }

    public StudentResponse updateStudentProfile(UpdateStudentRequest request, UUID userId) {
        Student student = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getStudent();
        student.setEmail(request.email());
        student.setPhone(request.phone());
        student.setAddress(request.address());
        StudentResponse response = studentToResponse(studentRepository.save(student));
        enrichStudentResponse(response);
        return response;
    }

    @Transactional
    public StudentResponse updateStudentAvatar(MultipartFile file, UUID userId) {
        Student student = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"))
                .getStudent();
        if(file != null && !file.isEmpty()){
            if(student.getAvatar() != null){
                fileAssetService.delete(student.getAvatar().getId(), userId);
            }
            student.setAvatar(fileAssetService.upload(file, userId));
            studentRepository.save(student);
        }
        StudentResponse response = studentToResponse(student);
        enrichStudentResponse(response);
        return response;
    }

    /* */

    /* LECTURER */

    public List<LecturerResponse> getLecturers(MentorSearchForm form, UUID userId){
        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }
        Specification<Lecturer> spec =
                MentorSpecification.search(form, organization.getId());

        return lecturerRepository
                .findAll(spec)
                .stream()
                .map(LecturerMapper::lecturerToResponse)
                .toList();
    }

    public Page<LecturerResponse> searchLecturers(MentorSearchForm form, Pageable pageable, UUID userId){
        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }
        Specification<Lecturer> spec = MentorSpecification.search(form, organization.getId());
        return lecturerRepository.findAll(spec, pageable)
                .map(LecturerMapper::lecturerToResponse);
    }

    public LecturerResponse createLecturer(CreateLecturerRequest request, UUID userId){
        Organization organization = getUserById(userId).getOrganization();
        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Lecturer lecturer = createLecturerRequestToLecturer(request);
        lecturer.setOrganization(organization);

        College college = null;
        Faculty faculty = null;
        Department department = null;

        if(request.collegeId() != null){
            college = collegeRepository.findByIdAndOrganization(request.collegeId(), organization)
                    .orElseThrow(() -> new BadRequestException("College not found"));
        }
        if(request.facultyId() != null){
            faculty = facultyRepository.findByIdAndOrganization(request.facultyId(), organization)
                    .orElseThrow(() -> new BadRequestException("Faculty not found"));
        }

        if(request.departmentId() != null){
            department = departmentRepository.findByIdAndOrganization(request.departmentId(), organization)
                    .orElseThrow(() -> new BadRequestException("Department not found"));
        }

        if(college != null && faculty != null){
            if(!college.getFaculties().contains(faculty)){
                throw new ConflictException("College does not contain faculty");
            }
        }

        if(faculty != null && department != null){
            if(!faculty.getDepartments().contains(department)){
                throw new ConflictException("Faculty does not contain department");
            }
        }

        if(college != null && faculty == null && department != null){
            throw new ConflictException("Invalid request");
        }

        lecturer.setCollege(college);
        lecturer.setFaculty(faculty);
        lecturer.setDepartment(department);

        User user = User.builder()
                .username(request.lecturerCode())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.LECTURER)
                .enabled(true)
                .build();

        lecturer.setUser(user);
        user.setLecturer(lecturer);

        userRepository.save(user);

        return lecturerToResponse(lecturer);
    }

    @Transactional
    public List<LecturerResponse> createLecturers(
            List<CreateLecturerRequest> requests,
            UUID userId
    ) {

        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Map<UUID, College> colleges = collegeRepository
                .findAllByOrganization(organization)
                .stream()
                .collect(Collectors.toMap(College::getId, c -> c));

        Map<UUID, Faculty> faculties = facultyRepository
                .findAllByOrganization(organization)
                .stream()
                .collect(Collectors.toMap(Faculty::getId, f -> f));

        Map<UUID, Department> departments = departmentRepository
                .findAllByOrganization(organization)
                .stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        List<User> users = new ArrayList<>();

        for (CreateLecturerRequest request : requests) {

            validateRequest(request);

            Lecturer lecturer = createLecturerRequestToLecturer(request);
            lecturer.setOrganization(organization);

            College college = request.collegeId() != null
                    ? colleges.get(request.collegeId())
                    : null;

            Faculty faculty = request.facultyId() != null
                    ? faculties.get(request.facultyId())
                    : null;

            Department department = request.departmentId() != null
                    ? departments.get(request.departmentId())
                    : null;

            if (request.collegeId() != null && college == null)
                throw new BadRequestException("College not found");

            if (request.facultyId() != null && faculty == null)
                throw new BadRequestException("Faculty not found");

            if (request.departmentId() != null && department == null)
                throw new BadRequestException("Department not found");

            validateHierarchy(college, faculty, department);

            lecturer.setCollege(college);
            lecturer.setFaculty(faculty);
            lecturer.setDepartment(department);

            User user = User.builder()
                    .username(request.lecturerCode())
                    .password(passwordEncoder.encode(request.password()))
                    .role(UserRole.LECTURER)
                    .enabled(true)
                    .build();

            lecturer.setUser(user);
            user.setLecturer(lecturer);

            users.add(user);
        }

        List<User> savedUsers = userRepository.saveAll(users);

        return savedUsers.stream()
                .map(User::getLecturer)
                .map(LecturerMapper::lecturerToResponse)
                .toList();
    }

    @Transactional
    public LecturerResponse updateLecturer(UUID id,
                                           UpdateLecturerRequest request,
                                           UUID userId) {

        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Lecturer lecturer = lecturerRepository
                .findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new BadRequestException("Lecturer not found"));

        // Update code
        if (request.lecturerCode() != null && !request.lecturerCode().equals(lecturer.getLecturerCode())) {

            if (lecturerRepository.existsByLecturerCodeAndOrganization(request.lecturerCode(), organization)) {
                throw new BadRequestException("Lecturer code already exists");
            }

            lecturer.setLecturerCode(request.lecturerCode());
            lecturer.getUser().setUsername(request.lecturerCode());
        }

        // Update fullName
        if (request.fullName() != null) {
            lecturer.setFullName(request.fullName());
        }

        // Update email
        if (request.email() != null && !request.email().equals(lecturer.getEmail())) {

            if (lecturerRepository.existsByEmailAndOrganization(request.email(), organization)) {
                throw new BadRequestException("Email already exists");
            }

            lecturer.setEmail(request.email());
        }

        // Update password
        if (request.password() != null && !request.password().isBlank()) {
            lecturer.getUser().setPassword(passwordEncoder.encode(request.password()));
        }

        // Update address
        if (request.address() != null) {
            lecturer.setAddress(request.address());
        }

        // Update phone
        if (request.phone() != null) {
            lecturer.setPhone(request.phone());
        }

        if (request.dob() != null) {
            lecturer.setDob(request.dob());
        }

        // Update
        College college = null;
        Faculty faculty = null;
        Department department = null;

        if(request.collegeId() != null){
            college = collegeRepository.findByIdAndOrganization(request.collegeId(), organization)
                    .orElseThrow(() -> new BadRequestException("College not found"));
        }
        if(request.facultyId() != null){
            faculty = facultyRepository.findByIdAndOrganization(request.facultyId(), organization)
                    .orElseThrow(() -> new BadRequestException("Faculty not found"));
        }

        if(request.departmentId() != null){
            department = departmentRepository.findByIdAndOrganization(request.departmentId(), organization)
                    .orElseThrow(() -> new BadRequestException("Department not found"));
        }

        if(college != null && faculty != null){
            if(!college.getFaculties().contains(faculty)){
                throw new ConflictException("College does not contain faculty");
            }
        }

        if(faculty != null && department != null){
            if(!faculty.getDepartments().contains(department)){
                throw new ConflictException("Faculty does not contain department");
            }
        }

        if(college != null && faculty == null && department != null){
            throw new ConflictException("Invalid request");
        }

        lecturer.setCollege(college);
        lecturer.setFaculty(faculty);
        lecturer.setDepartment(department);

        lecturerRepository.save(lecturer);

        return lecturerToResponse(lecturer);
    }

    @Transactional
    public void deleteLecturer(UUID id, UUID userId) {
        Organization organization = getUserById(userId).getOrganization();

        if (!organization.getManager().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission");
        }

        Lecturer lecturer = lecturerRepository.findByIdAndOrganization(id, organization)
                .orElseThrow(() -> new BadRequestException("Lecturer not found"));

        User user = lecturer.getUser();

        lecturer.setUser(null);
        user.setLecturer(null);

        lecturerRepository.delete(lecturer);
        userRepository.delete(user);
    }

    public LecturerResponse getLecturerProfile(UUID userId) {
        LecturerResponse response = lecturerToResponse(
                userRepository.findById(userId)
                        .orElseThrow(() -> new BadRequestException("Lecturer not found"))
                        .getLecturer()
        );
        enrichLecturerResponse(response);
        return response;
    }

    public LecturerResponse updateLecturerProfile(UpdateLecturerRequest request, UUID userId) {
        Lecturer lecturer = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Lecturer not found"))
                .getLecturer();
        lecturer.setEmail(request.email());
        lecturer.setAddress(request.address());
        lecturer.setDob(request.dob());
        lecturer.setPhone(request.phone());
        lecturerRepository.save(lecturer);
        return lecturerToResponse(lecturer);
    }

    @Transactional
    public LecturerResponse updateLecturerAvatar(MultipartFile file, UUID userId) {

        Lecturer lecturer = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Lecturer not found"))
                .getLecturer();

        if (file != null && !file.isEmpty()) {

            FileAsset oldAvatar = lecturer.getAvatar();

            FileAsset newAvatar = fileAssetService.upload(file, userId);

            lecturer.setAvatar(newAvatar);

            lecturerRepository.saveAndFlush(lecturer);

            if (oldAvatar != null) {
                fileAssetService.delete(oldAvatar.getId(), userId);
            }
        }

        LecturerResponse response = lecturerToResponse(lecturer);
        enrichLecturerResponse(response);

        return response;
    }

    /* */
    
    /* ADMIN */

    public Page<ManagerResponse> searchManagers(ManagerSearchForm form, Pageable pageable, UUID userId) {
        Specification<User> spec = ManagerSpecification.search(form);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if(!user.getRole().equals(UserRole.ADMIN)){
            throw new UnauthorizedException("You do not have permission");
        }
        return userRepository.findAll(spec, pageable)
                .map(UserMapper::managerToResponse)
                .map(response -> {
                    enrichManagerResponse(response);
                    return response;
                });
    }

    @Transactional
    public ManagerResponse createManager(ManagerRequest request, UUID userId) {

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!admin.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedException("You do not have permission");
        }

        if(organizationRepository.existsByCode(request.code())){
            throw new BadRequestException("Organization code already exists");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.MANAGER)
                .build();

        Organization organization = Organization.builder()
                .name(request.name())
                .code(request.code())
                .email(request.email())
                .phone(request.phone())
                .website(request.website())
                .type(request.type())
                .address(request.address())
                .description(request.description())
                .manager(user)
                .build();

        user.setOrganization(organization);

        userRepository.save(user);

        return UserMapper.managerToResponse(user);
    }

    @Transactional
    public ManagerResponse updateManager(UUID id, ManagerRequest request, UUID userId) {

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!admin.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedException("You do not have permission");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.getRole().equals(UserRole.MANAGER)) {
            throw new BadRequestException("User is not a manager");
        }

        Organization organization = user.getOrganization();

        if (organizationRepository.existsByCodeAndIdNot(request.code(), organization.getId())) {
            throw new BadRequestException("Organization code already exists");
        }

        // update organization
        organization.setName(request.name());
        organization.setCode(request.code());
        organization.setEmail(request.email());
        organization.setPhone(request.phone());
        organization.setWebsite(request.website());
        organization.setAddress(request.address());
        organization.setType(request.type());
        organization.setDescription(request.description());

        // update username nếu cần
        user.setUsername(request.username());

        return UserMapper.managerToResponse(user);
    }

    @Transactional
    public void deleteManager(UUID id, UUID userId) {

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!admin.getRole().equals(UserRole.ADMIN)) {
            throw new UnauthorizedException("You do not have permission");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!user.getRole().equals(UserRole.MANAGER)) {
            throw new BadRequestException("User is not a manager");
        }

        userRepository.delete(user);
    }
    
    /* */

    private void enrichStudentResponse(StudentResponse response){
        if (response.getAvatarUrl() != null && !response.getAvatarUrl().isBlank()) {
            response.setAvatarUrl(
                    storageService.generatePresignedUrl(response.getAvatarUrl())
            );
        }
    }

    private void enrichLecturerResponse(LecturerResponse response){
        if (response.getAvatarUrl() != null && !response.getAvatarUrl().isBlank()) {
            response.setAvatarUrl(
                    storageService.generatePresignedUrl(response.getAvatarUrl())
            );
        }
    }

    private void enrichManagerResponse(ManagerResponse response){
        if (response.getLogoUrl() != null && !response.getLogoUrl().isBlank()) {
            response.setLogoUrl(
                    storageService.generatePresignedUrl(response.getLogoUrl())
            );
        }
    }

    private void createOrganizationForManager(User manager){
        Organization organization = Organization.builder()
                .manager(manager)
                .code(generateOrgCode(manager))
                .name("Organization of " + manager.getUsername())
                .build();

        organizationRepository.save(organization);
    }

    private String generateOrgCode(User manager) {
        return null;
    }

    private void validateRequest(CreateLecturerRequest request) {

        if (request.lecturerCode() == null || request.lecturerCode().isBlank())
            throw new BadRequestException("Lecturer code required");

        if (request.fullName() == null || request.fullName().isBlank())
            throw new BadRequestException("Full name required");

        if (request.email() == null || request.email().isBlank())
            throw new BadRequestException("Email required");

        if (request.password() == null || request.password().isBlank())
            throw new BadRequestException("Password required");
    }

    private void validateHierarchy(
            College college,
            Faculty faculty,
            Department department
    ) {

        if (college != null && faculty != null) {
            if (!college.getFaculties().contains(faculty)) {
                throw new ConflictException("College does not contain faculty");
            }
        }

        if (faculty != null && department != null) {
            if (!faculty.getDepartments().contains(department)) {
                throw new ConflictException("Faculty does not contain department");
            }
        }

        if (college != null && faculty == null && department != null) {
            throw new ConflictException("Invalid organization hierarchy");
        }
    }
}
