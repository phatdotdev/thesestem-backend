package com.dev.thesis_management.specifications;

import com.dev.thesis_management.organization.entity.Department;
import com.dev.thesis_management.organization.entity.Faculty;
import com.dev.thesis_management.thesis.dto.council.CouncilSearchForm;
import com.dev.thesis_management.thesis.entity.Council;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CouncilSpecification {

    public static Specification<Council> filter(
            CouncilSearchForm form,
            UUID semesterId
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // semester
            predicates.add(
                    cb.equal(root.get("semester").get("id"), semesterId)
            );

            // LEFT JOIN
            Join<Council, Faculty> facultyJoin =
                    root.join("faculty", JoinType.LEFT);

            Join<Council, Department> departmentJoin =
                    root.join("department", JoinType.LEFT);

            Join<Department, Faculty> departmentFacultyJoin =
                    departmentJoin.join("faculty", JoinType.LEFT);

            // FILTER COLLEGE
            if (form.collegeId() != null) {

                Predicate councilCollege =
                        cb.equal(root.get("college").get("id"), form.collegeId());

                Predicate facultyCollege =
                        cb.equal(facultyJoin.get("college").get("id"), form.collegeId());

                Predicate departmentCollege =
                        cb.equal(
                                departmentFacultyJoin
                                        .get("college")
                                        .get("id"),
                                form.collegeId()
                        );

                predicates.add(
                        cb.or(
                                councilCollege,
                                facultyCollege,
                                departmentCollege
                        )
                );
            }

            // FILTER FACULTY
            if (form.facultyId() != null) {

                Predicate councilFaculty =
                        cb.equal(facultyJoin.get("id"), form.facultyId());

                Predicate departmentFaculty =
                        cb.equal(
                                departmentFacultyJoin.get("id"),
                                form.facultyId()
                        );

                predicates.add(
                        cb.or(councilFaculty, departmentFaculty)
                );
            }

            // FILTER DEPARTMENT
            if (form.departmentId() != null) {

                predicates.add(
                        cb.equal(
                                departmentJoin.get("id"),
                                form.departmentId()
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
