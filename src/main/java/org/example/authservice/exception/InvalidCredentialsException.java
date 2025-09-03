package org.example.authservice.exception;

public class InvalidCredentialsException extends ControllerException {
    public InvalidCredentialsException() {
        super(org.springframework.http.HttpStatus.UNAUTHORIZED, "Password is incorrect");
    }
}
