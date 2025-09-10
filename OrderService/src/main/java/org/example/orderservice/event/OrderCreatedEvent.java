package org.example.orderservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class OrderItemEvent {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class InvoiceGeneratedEvent {
    private Long orderId;
    private String invoiceNumber;
    private LocalDateTime invoiceDate;
    private BigDecimal totalAmount;
    private String pdfUrl;
}