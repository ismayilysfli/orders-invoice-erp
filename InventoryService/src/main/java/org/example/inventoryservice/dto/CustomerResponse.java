package org.example.inventoryservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
}
