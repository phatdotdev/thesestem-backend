package com.dev.thesis_management.specifications;

import com.dev.thesis_management.organization.dto.ManagerSearchForm;
import com.dev.thesis_management.organization.entity.Organization;
import com.dev.thesis_management.user.entity.User;
import com.dev.thesis_management.user.enums.UserRole;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ManagerSpecification {

    public static Specification<User> search(ManagerSearchForm form) {
        return (root, query, cb) -> {

            Join<User, Organization> orgJoin = root.join("organization", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("role"), UserRole.MANAGER));

            if (form.username() != null && !form.username().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("username")),
                        "%" + form.username().toLowerCase() + "%"
                ));
            }

            if (form.code() != null && !form.code().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(orgJoin.get("code")),
                        "%" + form.code().toLowerCase() + "%"
                ));
            }

            if (form.name() != null && !form.name().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(orgJoin.get("name")),
                        "%" + form.name().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}