package com.dev.thesis_management.organization.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrgRequest {
    String code;
    String name;
    String type;
    String description;
    String address;
    String email;
    String website;
    String phone;
}
