package com.dev.thesis_management.statistics.service;

import com.dev.thesis_management.exception.BadRequestException;
import com.dev.thesis_management.statistics.dto.mentor.MentorStatistics;
import com.dev.thesis_management.thesis.entity.MentorRegister;
import com.dev.thesis_management.thesis.repository.*;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class MentorStatisticsService {

    UserRepository userRepository;
    GroupRepository groupRepository;
    ThesisRepository thesisRepository;
    CouncilMemberRepository councilMemberRepository;
    DefenseScoreRepository defenseScoreRepository;
    MentorRegisterRepository mentorRegisterRepository;

    @Transactional(readOnly = true)
    public MentorStatistics getMentorStatisticsBySemester(UUID userId, UUID semesterId) {
        Lecturer lecturer = resolveLecturer(userId);

        long totalRegisteredStudents = mentorRegisterRepository.countDistinctStudentByMentorIdAndSemesterId(lecturer.getId(), semesterId);
        long totalAcceptedRegistrations = mentorRegisterRepository.countByMentorIdAndSemesterIdAndStatus(lecturer.getId(), semesterId, MentorRegister.Status.ACCEPTED);
        long totalRejectedRegistrations = mentorRegisterRepository.countByMentorIdAndSemesterIdAndStatus(lecturer.getId(), semesterId, MentorRegister.Status.REJECTED);
        long totalCancelledRegistrations = mentorRegisterRepository.countByMentorIdAndSemesterIdAndStatus(lecturer.getId(), semesterId, MentorRegister.Status.CANCELLED);

        long totalGroups = groupRepository.countBySemester_IdAndMentor_Id(semesterId, lecturer.getId());
        long totalTheses = thesisRepository.countByTopic_Group_Semester_IdAndTopic_Group_Mentor_Id(semesterId, lecturer.getId());
        long totalCouncils = councilMemberRepository.countDistinctCouncilByLecturerIdAndSemesterId(lecturer.getId(), semesterId);

        Double averageThesisScore = defenseScoreRepository.findAverageThesisScoreByMentorIdAndSemesterId(lecturer.getId(), semesterId);
        Double averageGivenScore = defenseScoreRepository.findAverageGivenScoreByLecturerIdAndSemesterId(lecturer.getId(), semesterId);
        return toMentorStatistics(totalRegisteredStudents, totalAcceptedRegistrations, totalRejectedRegistrations, totalCancelledRegistrations, totalTheses, totalGroups, totalCouncils, averageThesisScore, averageGivenScore);
    }

    @Transactional(readOnly = true)
    public MentorStatistics getMentorStatisticsAllSemesters(UUID userId) {
        Lecturer lecturer = resolveLecturer(userId);

        long totalRegisteredStudents = mentorRegisterRepository.countDistinctStudentByMentorId(lecturer.getId());
        long totalAcceptedRegistrations = mentorRegisterRepository.countByMentorIdAndStatus(lecturer.getId(), MentorRegister.Status.ACCEPTED);
        long totalRejectedRegistrations = mentorRegisterRepository.countByMentorIdAndStatus(lecturer.getId(), MentorRegister.Status.REJECTED);
        long totalCancelledRegistrations = mentorRegisterRepository.countByMentorIdAndStatus(lecturer.getId(), MentorRegister.Status.CANCELLED);

        long totalGroups = groupRepository.countByMentor_Id(lecturer.getId());
        long totalTheses = thesisRepository.countByTopic_Group_Mentor_Id(lecturer.getId());
        long totalCouncils = councilMemberRepository.countDistinctCouncilByLecturerId(lecturer.getId());

        Double averageThesisScore = defenseScoreRepository.findAverageThesisScoreByMentorId(lecturer.getId());
        Double averageGivenScore = defenseScoreRepository.findAverageGivenScoreByLecturerId(lecturer.getId());

        return toMentorStatistics(totalRegisteredStudents, totalAcceptedRegistrations, totalRejectedRegistrations, totalCancelledRegistrations, totalTheses, totalGroups, totalCouncils, averageThesisScore, averageGivenScore);
    }

    private Lecturer resolveLecturer(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getLecturer() == null) {
            throw new BadRequestException("Lecturer not found");
        }

        return user.getLecturer();
    }

    private MentorStatistics toMentorStatistics(
            long totalRegisteredStudents,
            long totalAcceptedRegistrations,
            long totalRejectedRegistrations,
            long totalCancelledRegistrations,
            long totalTheses,
            long totalGroups,
            long totalCouncils,
            Double averageThesisScore,
            Double averageGivenScore
    ) {
        MentorStatistics statistics = new MentorStatistics();
        statistics.setTotalRegisteredStudents(totalRegisteredStudents);
        statistics.setTotalAcceptedRegistrations(totalAcceptedRegistrations);
        statistics.setTotalRejectedRegistrations(totalRejectedRegistrations);
        statistics.setTotalCancelledRegistrations(totalCancelledRegistrations);
        statistics.setTotalTheses(totalTheses);
        statistics.setTotalGroups(totalGroups);
        statistics.setTotalCouncils(totalCouncils);
        statistics.setAverageThesisScore(averageThesisScore != null ? averageThesisScore : 0.0d);
        statistics.setAverageGivenScore(averageGivenScore != null ? averageGivenScore : 0.0d);
        return statistics;
    }
}
