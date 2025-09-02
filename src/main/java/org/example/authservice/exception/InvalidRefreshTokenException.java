package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ControllerException {
    public InvalidRefreshTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}

