package com.dev.thesis_management.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticatedException(
            UnauthenticatedException e,
            HttpServletRequest request){
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", e.getMessage(),  request.getRequestURI());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException e,
            HttpServletRequest request){
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage(),  request.getRequestURI());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException e,
            HttpServletRequest request){
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage(),  request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException e,
            HttpServletRequest request){
        return buildResponse(HttpStatus.CONFLICT, "CONFLICT", e.getMessage(),  request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception e,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", e.getMessage(), request.getRequestURI());
    }


    /* ================= BUILDER ================= */

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            String path
    ) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .code(code)
                        .message(message)
                        .path(path)
                        .build());
    }

    /* ================= DTO ================= */

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
