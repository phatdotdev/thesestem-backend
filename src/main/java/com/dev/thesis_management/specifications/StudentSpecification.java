package com.dev.thesis_management.specifications;

import com.dev.thesis_management.user.dto.StudentSearchForm;
import com.dev.thesis_management.user.entity.Student;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StudentSpecification {

    public static Specification<Student> search(
            StudentSearchForm form,
            UUID orgId
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                    cb.equal(root.get("organization").get("id"), orgId)
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
                                cb.lower(root.get("studentCode")),
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

            if (form.programId() != null) {
                predicates.add(
                        cb.equal(root.get("program").get("id"), form.programId())
                );
            }

            if(form.courseId() != null){
                predicates.add(
                        cb.equal(root.join("course").get("id"), form.courseId())
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}