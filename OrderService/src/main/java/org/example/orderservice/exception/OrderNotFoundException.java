package org.example.orderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends ControllerException {
    public OrderNotFoundException(Long orderId) {
        super(HttpStatus.NOT_FOUND, "Order not found with id: " + orderId);
    }
}