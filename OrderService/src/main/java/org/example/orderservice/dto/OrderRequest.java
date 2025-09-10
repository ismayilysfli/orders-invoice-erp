package org.example.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "At least one order item is required")
    private List<OrderItemRequest> items;


}
