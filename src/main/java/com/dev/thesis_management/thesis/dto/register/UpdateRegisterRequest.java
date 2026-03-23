package com.dev.thesis_management.thesis.dto.register;

import com.dev.thesis_management.thesis.entity.MentorRegister;

public record UpdateRegisterRequest(
        MentorRegister.Status status,
        String response
) {
}
