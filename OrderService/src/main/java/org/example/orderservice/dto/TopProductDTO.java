package org.example.orderservice.dto;

import java.math.BigDecimal;

public record TopProductDTO(Long productId, Long totalQuantity, BigDecimal totalRevenue) {}

