package org.example.orderservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ControllerException.class)
    public ResponseEntity<ErrorMessage> HandleException(ControllerException exception){
        return ResponseEntity.status(exception.getStatus())
                .body(new ErrorMessage(exception.getMessage(), exception.getStatus()));
    }

}
