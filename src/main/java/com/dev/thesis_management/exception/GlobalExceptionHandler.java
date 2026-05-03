package com.dev.thesis_management.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnauthenticatedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthenticatedException(
            UnauthenticatedException e,
            HttpServletRequest request){
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHENTICATED",
                resolveMessage(e, "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn."),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException e,
            HttpServletRequest request){
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                resolveMessage(e, "Bạn không có quyền thực hiện thao tác này."),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException e,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                resolveMessage(e, "Bạn không có quyền thực hiện thao tác này."),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException e,
            HttpServletRequest request){
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                resolveMessage(e, "Dữ liệu gửi lên không hợp lệ."),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException e,
            HttpServletRequest request){
        return buildResponse(
                HttpStatus.CONFLICT,
                "CONFLICT",
                resolveMessage(e, "Dữ liệu bị xung đột, vui lòng kiểm tra lại."),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException e,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                resolveMessage(e, "Không tìm thấy dữ liệu yêu cầu."),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException e,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                resolveMessage(e, "Tham số không hợp lệ."),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception e,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                resolveMessage(e, "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."),
                request.getRequestURI()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String path = extractPath(request);
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(this::toValidationMessage)
                .orElse("Dữ liệu gửi lên không hợp lệ.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("BAD_REQUEST")
                        .message(message)
                        .path(path)
                        .build());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String path = extractPath(request);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("NOT_FOUND")
                        .message("Không tìm thấy đường dẫn yêu cầu.")
                        .path(path)
                        .build());
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

    private String resolveMessage(Exception e, String fallback) {
        String message = e.getMessage();
        return (message == null || message.isBlank()) ? fallback : message;
    }

    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.startsWith("uri=") ? description.substring(4) : description;
    }

    private String toValidationMessage(FieldError fieldError) {
        String defaultMessage = Objects.requireNonNullElse(fieldError.getDefaultMessage(), "không hợp lệ");
        return "%s: %s".formatted(fieldError.getField(), defaultMessage);
    }

    /* ================= DTO ================= */

    @Data
    @Builder
    public static class ErrorResponse {
        LocalDateTime timestamp;
        int status;
        String code;
        String message;
        String path;
    }
}
