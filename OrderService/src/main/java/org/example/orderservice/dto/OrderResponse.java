package org.example.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.orderservice.model.OrderItem;
import org.example.orderservice.model.OrderStatus;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse  {
    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String customerId;
    private OrderStatus status;
    private List<OrderItemResponse> items;
}
