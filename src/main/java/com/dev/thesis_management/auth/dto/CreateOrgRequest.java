package com.dev.thesis_management.auth.dto;

import lombok.Data;

@Data
public class CreateOrgRequest {
    String email;
    String orgName;
    String orgCode;
    String orgEmail;
    String orgPhone;
    String orgAddress;
    String orgType;
}
