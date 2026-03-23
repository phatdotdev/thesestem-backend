package com.dev.thesis_management.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String code = "UNAUTHORIZED";
        String message = "Authentication required";

        if (ex instanceof OAuth2AuthenticationException oauth2Ex) {
            OAuth2Error error = oauth2Ex.getError();

            switch (error.getErrorCode()) {
                case "invalid_token" -> {
                    code = "JWT_INVALID";
                    message = error.getDescription();
                }
                case "invalid_request" -> {
                    code = "JWT_MISSING";
                    message = "JWT token is missing";
                }
                default -> {
                    code = error.getErrorCode();
                    message = error.getDescription();
                }
            }
        }

        response.getWriter().write("""
    {
      "timestamp": "%s",
      "status": 401,
      "code": "%s",
      "message": "%s",
      "path": "%s"
    }
    """.formatted(
                java.time.LocalDateTime.now(),
                code,
                message,
                request.getRequestURI()
        ));
    }

}



