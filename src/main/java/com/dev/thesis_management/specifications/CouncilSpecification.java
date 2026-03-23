package com.dev.thesis_management.specifications;

import com.dev.thesis_management.thesis.dto.council.CouncilSearchForm;
import com.dev.thesis_management.thesis.entity.Council;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CouncilSpecification {

    public static Specification<Council> filter(CouncilSearchForm form, UUID semesterId) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (form.name() != null && !form.name().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + form.name().toLowerCase() + "%"
                        )
                );
            }

            if (form.code() != null && !form.code().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("code")),
                                "%" + form.code().toLowerCase() + "%"
                        )
                );
            }

            if (semesterId != null) {
                predicates.add(
                        cb.equal(
                                root.get("semester").get("id"),
                                semesterId
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
