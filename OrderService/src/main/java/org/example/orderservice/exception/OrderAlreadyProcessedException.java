package org.example.orderservice.exception;

import org.springframework.http.HttpStatus;

public class OrderAlreadyProcessedException extends ControllerException {
    public OrderAlreadyProcessedException(Long orderId, String currentStatus) {
        super(HttpStatus.CONFLICT,
                String.format("Order %d is already %s and cannot be modified", orderId, currentStatus));
    }
}