package com.dev.thesis_management.specifications;

import com.dev.thesis_management.thesis.dto.defense.DefenseSearchForm;
import com.dev.thesis_management.thesis.entity.ThesisDefense;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ThesisDefenseSpecification {

    public static Specification<ThesisDefense> search(DefenseSearchForm form, UUID semesterId) {
        return search(form, semesterId, null);
    }

    public static Specification<ThesisDefense> search(
            DefenseSearchForm form,
            UUID semesterId,
            UUID organizationId
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // join thesis -> topic -> group -> semester
            Join<Object, Object> thesis = root.join("thesis");
            Join<Object, Object> topic = thesis.join("topic");
            Join<Object, Object> group = topic.join("group");

            // join thesis -> student -> program hierarchy (allow null relationship)
            Join<Object, Object> student = thesis.join("student", JoinType.LEFT);
            Join<Object, Object> program = student.join("program", JoinType.LEFT);
            Join<Object, Object> department = program.join("department", JoinType.LEFT);
            Join<Object, Object> faculty = program.join("faculty", JoinType.LEFT);
            Join<Object, Object> college = program.join("college", JoinType.LEFT);
            Join<Object, Object> departmentFaculty = department.join("faculty", JoinType.LEFT);

            if (organizationId != null) {
                predicates.add(cb.equal(student.get("organization").get("id"), organizationId));
            }

            // filter semester
            if (semesterId != null) {

                Join<Object, Object> semester = group.join("semester");

                predicates.add(
                        cb.equal(semester.get("id"), semesterId)
                );
            }

            if (form == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // filter council
            if (form.councilId() != null || (form.councilCode() != null && !form.councilCode().isBlank())) {

                Join<Object, Object> council = root.join("council");

                if (form.councilId() != null) {
                    predicates.add(cb.equal(council.get("id"), form.councilId()));
                }

                if (form.councilCode() != null && !form.councilCode().isBlank()) {
                    String councilCode = "%" + form.councilCode().toLowerCase() + "%";
                    predicates.add(cb.like(cb.lower(council.get("code")), councilCode));
                }
            }

            if (form.programId() != null) {
                predicates.add(cb.equal(program.get("id"), form.programId()));
            }

            if (form.departmentId() != null) {
                predicates.add(cb.equal(department.get("id"), form.departmentId()));
            }

            if (form.facultyId() != null) {
                predicates.add(
                        cb.or(
                                cb.equal(faculty.get("id"), form.facultyId()),
                                cb.equal(departmentFaculty.get("id"), form.facultyId())
                        )
                );
            }

            if (form.collegeId() != null) {
                predicates.add(
                        cb.or(
                                cb.equal(college.get("id"), form.collegeId()),
                                cb.equal(faculty.get("college").get("id"), form.collegeId()),
                                cb.equal(departmentFaculty.get("college").get("id"), form.collegeId())
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}