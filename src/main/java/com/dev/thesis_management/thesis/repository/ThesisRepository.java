package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Thesis;
import com.dev.thesis_management.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ThesisRepository extends JpaRepository<Thesis, UUID>, JpaSpecificationExecutor<Thesis> {
    List<Thesis> findAllByTopic_Group_Id(UUID groupId);
    List<Thesis> findAllByTopic_Group_Semester_IdAndStudent(UUID semesterId, Student student);
}
