package com.dev.thesis_management.auth.controller;

import com.dev.thesis_management.auth.dto.AuthResponse;
import com.dev.thesis_management.auth.dto.LoginRequest;
import com.dev.thesis_management.auth.dto.RegisterRequest;
import com.dev.thesis_management.auth.dto.VerifyRequest;
import com.dev.thesis_management.auth.service.AuthService;
import com.dev.thesis_management.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.dev.thesis_management.common.utils.ResponseEntityUtils.*;
import static com.dev.thesis_management.common.utils.UUIDUtils.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {

    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request){
        AuthResponse response = authService.login(request);
        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refresh_token",
                        response.getRefreshToken()
                )
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ok(response, headers);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ){
        return ok(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            HttpServletResponse response,
            Authentication authentication) {

        ResponseCookie deleteCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        authService.logout(parseUUID(authentication.getName()));
        return noContent();
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @RequestBody RegisterRequest request
            ){
        authService.register(request);
        return noContent();
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse> verify(
            @RequestBody VerifyRequest request
    ){
        authService.verify(request);
        return noContent();
    }

    @PostMapping("/{code}/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginToOrg(
            @PathVariable String code,
            @RequestBody LoginRequest request
    ){
        AuthResponse response = authService.loginToOrg(code, request);
        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refresh_token",
                        response.getRefreshToken()
                )
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ok(response, headers);
    }

}
