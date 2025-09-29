package org.example.orderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_events")
@Getter
@Setter
public class OrderEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private Long orderId;

    @Lob
    private String payload;

    private LocalDateTime createdAt;
    private boolean processed;
    private LocalDateTime processedAt;

    @PrePersist
    void prePersist(){
        if(createdAt == null) createdAt = LocalDateTime.now();
    }
}

