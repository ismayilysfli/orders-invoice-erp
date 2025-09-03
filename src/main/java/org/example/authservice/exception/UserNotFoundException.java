package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ControllerException {
    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}

