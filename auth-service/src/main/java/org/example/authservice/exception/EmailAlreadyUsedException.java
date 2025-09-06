package org.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyUsedException extends ControllerException {
    public EmailAlreadyUsedException() {
        super(HttpStatus.CONFLICT, "Email already registered");
    }
}

