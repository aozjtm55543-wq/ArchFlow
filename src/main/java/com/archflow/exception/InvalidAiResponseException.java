package com.archflow.exception;

public class InvalidAiResponseException extends RuntimeException {
    public InvalidAiResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
