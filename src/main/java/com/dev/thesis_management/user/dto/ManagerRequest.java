package com.dev.thesis_management.user.dto;

import com.dev.thesis_management.user.enums.OrganizationType;

public record ManagerRequest(
        String username,
        String password,
        String name,
        String code,
        String email,
        String phone,
        String address,
        String website,
        String description,
        OrganizationType type
) {
}
