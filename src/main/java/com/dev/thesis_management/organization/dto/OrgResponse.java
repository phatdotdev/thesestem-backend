package com.dev.thesis_management.organization.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrgResponse {
    UUID id;
    String code;
    String name;
    String email;
    String website;
    String phone;
    String type;
    String description;
    String address;
    String managerEmail;
    String logoUrl;
    String coverUrl;
    String bannerUrl;
    String role;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
