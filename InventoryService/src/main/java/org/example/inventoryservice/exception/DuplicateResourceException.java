package org.example.inventoryservice.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ControllerException{
    public DuplicateResourceException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
