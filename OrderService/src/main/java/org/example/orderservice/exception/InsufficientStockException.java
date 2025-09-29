package org.example.orderservice.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends ControllerException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super(HttpStatus.BAD_REQUEST,
                String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                        productId, requested, available));
    }
}