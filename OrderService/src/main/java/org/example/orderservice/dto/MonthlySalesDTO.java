package org.example.orderservice.dto;

import java.math.BigDecimal;

public record MonthlySalesDTO(int year, int month, BigDecimal totalSales, long orderCount, BigDecimal avgOrderValue) {}

