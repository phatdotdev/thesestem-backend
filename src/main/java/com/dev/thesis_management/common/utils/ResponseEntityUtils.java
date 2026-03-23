package com.dev.thesis_management.common.utils;

import com.dev.thesis_management.common.response.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseEntityUtils {

    /* SUCCESS */

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return build(HttpStatus.OK, "Success", data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, HttpHeaders headers) {
        return build(HttpStatus.OK, "Success", data, headers);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(
            String message,
            T data,
            HttpHeaders headers
    ) {
        return build(HttpStatus.OK, message, data, headers);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return build(HttpStatus.OK, message, data);
    }

    public static ResponseEntity<ApiResponse<Void>> ok(String message) {
        return build(HttpStatus.OK, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return build(HttpStatus.CREATED, "Created successfully", data);
    }

    public static ResponseEntity<ApiResponse> noContent(){
        return ResponseEntity.noContent().build();
    }

    /* ERROR */

    public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return build(HttpStatus.BAD_REQUEST, message, null);
    }

    public static ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        return build(HttpStatus.UNAUTHORIZED, message, null);
    }

    public static ResponseEntity<ApiResponse<Void>> forbidden(String message) {
        return build(HttpStatus.FORBIDDEN, message, null);
    }

    public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return build(HttpStatus.NOT_FOUND, message, null);
    }

    public static ResponseEntity<ApiResponse<Void>> internalError(String message) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, null);
    }

    /* COMMON */

    private static <T> ResponseEntity<ApiResponse<T>> build(
            HttpStatus status,
            String message,
            T data
    ){
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
        return  ResponseEntity.status(status).body(response);
    }

    private static <T> ResponseEntity<ApiResponse<T>> build(
            HttpStatus status,
            String message,
            T data,
            HttpHeaders headers
    ) {
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();

        return ResponseEntity
                .status(status)
                .headers(headers)
                .body(response);
    }

}
