package com.dev.thesis_management.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManagerResponse {
    UUID id;
    String username;
    String name;
    String code;
    String email;
    String phone;
    String address;
    String website;
    String description;
    String logoUrl;
    String type;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
