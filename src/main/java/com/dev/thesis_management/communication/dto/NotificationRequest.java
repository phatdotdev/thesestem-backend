package com.dev.thesis_management.communication.dto;

import com.dev.thesis_management.user.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record NotificationRequest(
        UUID id,
        @NotBlank(message = "Tiêu đề thông báo không được để trống")
        @Size(max = 255, message = "Tiêu đề thông báo tối đa 255 ký tự")
        String title,
        @NotBlank(message = "Nội dung thông báo không được để trống")
        String content,
        UserRole role
) {
}
