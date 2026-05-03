package com.dev.thesis_management.thesis.service;

import com.dev.thesis_management.communication.entity.Notification;
import com.dev.thesis_management.communication.service.NotificationService;
import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.organization.service.OrgService;
import com.dev.thesis_management.thesis.dto.CreateRegisterRequest;
import com.dev.thesis_management.thesis.dto.RegisterResponse;
import com.dev.thesis_management.thesis.dto.register.UpdateRegisterRequest;
import com.dev.thesis_management.thesis.entity.MentorRegister;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.thesis.mapper.RegisterMapper;
import com.dev.thesis_management.thesis.repository.MentorRegisterRepository;
import com.dev.thesis_management.thesis.repository.SemesterRepository;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.repository.LecturerRepository;
import com.dev.thesis_management.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegisterService {

    UserRepository userRepository;
    LecturerRepository lecturerRepository;
    SemesterRepository semesterRepository;
    MentorRegisterRepository mentorRegisterRepository;
    NotificationService notificationService;

    OrgService orgService;

    @Transactional
    public RegisterResponse createRegister(CreateRegisterRequest request, UUID userId) {

        User user = getUserById(userId);

        Organization organization = orgService.findByUserId(userId);
        Semester semester = getCurrentSemester(organization);

        Student student = user.getStudent();
        if (student == null) {
            throw new BadRequestException("Student not found");
        }

        Lecturer mentor = lecturerRepository
                .findByIdAndOrganization(request.mentorId(), organization)
                .orElseThrow(() -> new BadRequestException("Lecturer not found"));

        if(mentorRegisterRepository.existsByStudentIdAndMentorIdAndSemesterId(student.getId(), mentor.getId(), semester.getId())){
            throw new BadRequestException("You already registered this mentor");
        }

        MentorRegister register = MentorRegister.builder()
                .student(student)
                .semester(semester)
                .mentor(mentor)
                .status(MentorRegister.Status.PENDING)
                .message(request.message())
                .build();

        mentorRegisterRepository.save(register);

        String studentName = student.getFullName() != null ? student.getFullName() : student.getStudentCode();
        notificationService.notifyUser(
                mentor.getUser(),
                "Yêu cầu hướng dẫn mới",
                studentName + " đã đăng ký bạn hướng dẫn trong học kỳ hiện tại.",
                Notification.Type.STUDENT_REGISTER_MENTOR
        );

        return RegisterResponse.builder()
                .build();
    }

    @Transactional
    public List<RegisterResponse> getMentorRegisters(UUID mentorId){
        Lecturer mentor = getUserById(mentorId).getLecturer();
        if(mentor == null){
            throw new BadRequestException("You are not mentor");
        }
        Organization organization = orgService.findByUserId(mentorId);
        Semester semester = getCurrentSemester(organization);
        return mentorRegisterRepository.findAllByMentorIdAndSemesterId(mentor.getId(), semester.getId())
                .stream().map(RegisterMapper::toRegisterResponse).toList();
    }

    @Transactional
    public List<RegisterResponse> getStudentRegisters(UUID studentId){
        Student student = getUserById(studentId).getStudent();
        Organization organization = orgService.findByUserId(studentId);
        Semester semester = getCurrentSemester(organization);
        return mentorRegisterRepository.findAllByStudentIdAndSemesterId(student.getId(), semester.getId())
                .stream().map(RegisterMapper::toRegisterResponse).toList();
    }

    @Transactional
    public RegisterResponse updateRegisterStatus(UUID id, UpdateRegisterRequest request, UUID userId) {

        MentorRegister register = mentorRegisterRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Register not found"));

        MentorRegister.Status status = request.status();
        if (register.getStatus() != MentorRegister.Status.PENDING) {
            throw new BadRequestException("Only pending register can be updated");
        }

        boolean isMentor = register.getMentor().getUser().getId().equals(userId);
        boolean isStudent = register.getStudent().getUser().getId().equals(userId);

        if (!isMentor && !isStudent) {
            throw new BadRequestException("You are not allowed to update this register");
        }

        if (isMentor) {
            if (status != MentorRegister.Status.ACCEPTED &&
                    status != MentorRegister.Status.REJECTED) {
                throw new BadRequestException("Mentor can only ACCEPT or REJECT");
            }
            register.setResponse(request.response());
        }

        if (isStudent) {
            if (status != MentorRegister.Status.CANCELLED) {
                throw new BadRequestException("Student can only CANCEL");
            }
        }

        register.setStatus(status);

        mentorRegisterRepository.save(register);

        if (isMentor) {
            Notification.Type type = status == MentorRegister.Status.ACCEPTED
                    ? Notification.Type.MENTOR_APPROVE_REGISTER
                    : Notification.Type.MENTOR_REJECT_REGISTER;

            String mentorName = register.getMentor().getFullName() != null
                    ? register.getMentor().getFullName()
                    : register.getMentor().getLecturerCode();

            notificationService.notifyUser(
                    register.getStudent().getUser(),
                    "Cập nhật đăng ký hướng dẫn",
                    "Giảng viên " + mentorName + " đã " + (status == MentorRegister.Status.ACCEPTED ? "chấp nhận" : "từ chối") + " yêu cầu đăng ký của bạn.",
                    type
            );
        }

        if (isStudent) {
            String studentName = register.getStudent().getFullName() != null
                    ? register.getStudent().getFullName()
                    : register.getStudent().getStudentCode();

            notificationService.notifyUser(
                    register.getMentor().getUser(),
                    "Hủy đăng ký hướng dẫn",
                    studentName + " đã hủy yêu cầu đăng ký hướng dẫn.",
                    Notification.Type.STUDENT_CANCEL_REGISTER
            );
        }

        return RegisterMapper.toRegisterResponse(register);
    }

    /* HELPER METHODS */

    public Semester getCurrentSemester(Organization organization){
        return semesterRepository.findByOrganizationAndStatus(organization, Semester.Status.ACTIVE)
                .orElseThrow(() -> new BadRequestException("Semester not found"));
    }

    public User getUserById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
