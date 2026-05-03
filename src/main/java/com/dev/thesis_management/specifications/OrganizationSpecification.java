package com.dev.thesis_management.specifications;

import com.dev.thesis_management.organization.dto.organization.OrganizationSearchForm;
import com.dev.thesis_management.organization.entity.Organization;
import org.springframework.data.jpa.domain.Specification;

public class OrganizationSpecification {

    public static Specification<Organization> build(OrganizationSearchForm form) {

        return Specification
                .where(hasName(form.name()))
                .and(hasCode(form.code()));
    }

    public static Specification<Organization> hasName(String name) {
        return (root, query, cb) -> {

            if (name == null || name.isBlank()) {
                return null;
            }

            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    public static Specification<Organization> hasCode(String code) {
        return (root, query, cb) -> {

            if (code == null || code.isBlank()) {
                return null;
            }

            return cb.like(
                    cb.lower(root.get("code")),
                    "%" + code.toLowerCase() + "%"
            );
        };
    }
}