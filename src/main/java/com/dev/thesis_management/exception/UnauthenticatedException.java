package com.dev.thesis_management.exception;

public class UnauthenticatedException extends RuntimeException{
    public UnauthenticatedException(String message){
        super(message);
    }
}
