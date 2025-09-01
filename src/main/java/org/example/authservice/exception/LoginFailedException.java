package org.example.authservice.exception;

public class LoginFailedException extends ControllerException {
    public LoginFailedException() {
        super(org.springframework.http.HttpStatus.UNAUTHORIZED, "Password is incorrect");
    }
}
