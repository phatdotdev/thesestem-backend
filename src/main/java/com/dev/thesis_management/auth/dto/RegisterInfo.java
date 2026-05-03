package com.dev.thesis_management.auth.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterInfo {
    String email;
    String password;
    String code;
    boolean verified;
}
