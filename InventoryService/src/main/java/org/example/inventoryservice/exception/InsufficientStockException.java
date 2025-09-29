package org.example.inventoryservice.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends ControllerException {
    public InsufficientStockException(Long productId, int requested, int available) {
        super(HttpStatus.BAD_REQUEST, "Insufficient stock for product " + productId + ": requested=" + requested + ", available=" + available);
    }
}

