package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class ErrorMessage {
    private final String message;
    private final int status;

    public ErrorMessage(String message, HttpStatus status) {
        this.message = message;
        this.status = status.value();
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() { // Change return type to int
        return status;
    }
}