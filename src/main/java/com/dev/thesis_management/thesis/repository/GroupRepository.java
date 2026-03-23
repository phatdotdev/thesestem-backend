package com.dev.thesis_management.thesis.repository;

import com.dev.thesis_management.thesis.entity.Group;
import com.dev.thesis_management.thesis.entity.Semester;
import com.dev.thesis_management.user.entity.Lecturer;
import com.dev.thesis_management.user.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findAllBySemesterAndMentor(Semester semester, Lecturer mentor);

    List<Group> findAllBySemesterAndStudentsContains(Semester semester, Student student);
}
