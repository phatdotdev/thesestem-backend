package com.dev.thesis_management.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String code = "UNAUTHORIZED";
        String message = "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.";

        if (ex instanceof OAuth2AuthenticationException oauth2Ex) {
            OAuth2Error error = oauth2Ex.getError();

            switch (error.getErrorCode()) {
                case "invalid_token" -> {
                    code = "JWT_INVALID";
                    message = "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.";
                }
                case "invalid_request" -> {
                    code = "JWT_MISSING";
                    message = "Thiếu thông tin xác thực.";
                }
                default -> {
                    code = error.getErrorCode();
                    message = "Không thể xác thực người dùng.";
                }
            }
        }

        ErrorResponse payload = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(payload));
    }

    @Data
    @Builder
    static class ErrorResponse {
        LocalDateTime timestamp;
        int status;
        String code;
        String message;
        String path;
    }

}



