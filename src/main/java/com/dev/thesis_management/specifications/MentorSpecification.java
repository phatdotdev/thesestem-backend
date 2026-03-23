package com.dev.thesis_management.specifications;


import com.dev.thesis_management.user.dto.MentorSearchForm;
import com.dev.thesis_management.user.entity.Lecturer;
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

            predicates.add(
                    cb.equal(root.get("organization").get("id"), organizationId)
            );

            if (form == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            if (form.name() != null && !form.name().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("fullName")),
                                "%" + form.name().toLowerCase() + "%"
                        )
                );
            }

            if (form.code() != null && !form.code().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("lecturerCode")),
                                "%" + form.code().toLowerCase() + "%"
                        )
                );
            }

            if (form.email() != null && !form.email().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("email")),
                                "%" + form.email().toLowerCase() + "%"
                        )
                );
            }

            if (form.collegeId() != null) {
                predicates.add(
                        cb.equal(root.get("college").get("id"), form.collegeId())
                );
            }

            if (form.facultyId() != null) {
                predicates.add(
                        cb.equal(root.get("faculty").get("id"), form.facultyId())
                );
            }

            if (form.departmentId() != null) {
                predicates.add(
                        cb.equal(root.get("department").get("id"), form.departmentId())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}