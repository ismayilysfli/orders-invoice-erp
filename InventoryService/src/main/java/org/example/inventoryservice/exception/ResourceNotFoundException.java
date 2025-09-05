package org.example.inventoryservice.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ControllerException{
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
