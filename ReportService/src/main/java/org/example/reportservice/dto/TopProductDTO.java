package org.example.reportservice.dto;

import java.math.BigDecimal;

public record TopProductDTO(Long productId, Long totalQuantity, BigDecimal totalRevenue) {}

