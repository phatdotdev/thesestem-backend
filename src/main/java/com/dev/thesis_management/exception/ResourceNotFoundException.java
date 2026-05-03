package com.dev.thesis_management.exception;

public class ResourceNotFoundException extends RuntimeException{
	public ResourceNotFoundException(String message) {
		super(message);
	}

	public ResourceNotFoundException() {
		super("Không tìm thấy dữ liệu yêu cầu.");
	}
}
