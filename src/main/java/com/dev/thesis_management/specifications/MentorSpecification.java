package com.dev.thesis_management.specifications;


import com.dev.thesis_management.user.dto.MentorSearchForm;
import com.dev.thesis_management.user.entity.Lecturer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MentorSpecification {

    public static Specification<Lecturer> search(
            MentorSearchForm form,
            UUID organizationId
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("organization").get("id"), organizationId));

            if (form == null) return cb.and(predicates.toArray(new Predicate[0]));

            if (form.name() != null && !form.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("fullName")), "%" + form.name().toLowerCase() + "%"));
            }
            if (form.code() != null && !form.code().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("lecturerCode")), "%" + form.code().toLowerCase() + "%"));
            }

            Join<Object, Object> department = root.join("department", JoinType.LEFT);
            Join<Object, Object> faculty = root.join("faculty", JoinType.LEFT);
            Join<Object, Object> college = root.join("college", JoinType.LEFT);

            Join<Object, Object> deptFaculty = department.join("faculty", JoinType.LEFT);

            if (form.collegeId() != null) {
                predicates.add(cb.or(
                        cb.equal(college.get("id"), form.collegeId()),
                        cb.equal(faculty.get("college").get("id"), form.collegeId()),
                        cb.equal(deptFaculty.get("college").get("id"), form.collegeId())
                ));
            }

            if (form.facultyId() != null) {
                predicates.add(cb.or(
                        cb.equal(faculty.get("id"), form.facultyId()),
                        cb.equal(deptFaculty.get("id"), form.facultyId())
                ));
            }

            if (form.departmentId() != null) {
                predicates.add(cb.equal(department.get("id"), form.departmentId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}