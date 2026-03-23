package com.dev.thesis_management.user.mapper;

import com.dev.thesis_management.user.dto.ManagerResponse;
import com.dev.thesis_management.user.entity.User;

public class UserMapper {
    public static ManagerResponse managerToResponse(User user){
        return ManagerResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .code(user.getOrganization().getCode())
                .name(user.getOrganization().getName())
                .email(user.getOrganization().getEmail())
                .phone(user.getOrganization().getPhone())
                .website(user.getOrganization().getWebsite())
                .address(user.getOrganization().getAddress())
                .logoUrl(user.getOrganization() != null
                        ? user.getOrganization().getLogo() != null
                        ? user.getOrganization().getLogo().getPath()
                        : null
                        : null)
                .type(user.getOrganization().getType().name())
                .description(user.getOrganization().getDescription())
                .build();
    }
}
