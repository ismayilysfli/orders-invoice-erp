package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class ControllerException extends RuntimeException {
    private HttpStatus status;
    public ControllerException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
    public HttpStatus getStatus() {
        return status;
    }

}