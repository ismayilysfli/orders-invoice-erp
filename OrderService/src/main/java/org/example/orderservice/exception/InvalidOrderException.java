package org.example.orderservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidOrderException extends ControllerException {
    public InvalidOrderException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}