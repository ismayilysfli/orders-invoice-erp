package org.example.reportservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({IllegalArgumentException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ReportError> badRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ReportError> validation(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ReportError> generic(Exception ex) {
        log.warn("Unhandled exception: {}", ex.getMessage());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error");
    }

    private ResponseEntity<ReportError> build(HttpStatus status, String msg) {
        return ResponseEntity.status(status)
                .body(new ReportError(Instant.now().toString(), status.value(), status.getReasonPhrase(), msg));
    }

    public record ReportError(String timestamp, int status, String error, String message) {}
}

