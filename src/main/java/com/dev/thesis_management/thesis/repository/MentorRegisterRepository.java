package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.MentorRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MentorRegisterRepository extends JpaRepository<MentorRegister, UUID> {
    boolean existsByStudentIdAndSemesterId(UUID id, UUID id1);
    boolean existsByStudentIdAndMentorIdAndSemesterId(UUID stdId, UUID mentorId, UUID semesterId);

    List<MentorRegister> findAllByStudentIdAndSemesterId(UUID studentId, UUID semesterId);
    List<MentorRegister> findAllByMentorIdAndSemesterId(UUID mentorId, UUID semesterId);

    long countDistinctStudentByMentorId(UUID id);
    long countDistinctStudentByMentorIdAndSemesterId(UUID mentorId, UUID semesterId);
    long countByMentorIdAndStatus(UUID mentorId, MentorRegister.Status status);
    long countByMentorIdAndSemesterIdAndStatus(UUID mentorId, UUID semesterId, MentorRegister.Status status);
}
