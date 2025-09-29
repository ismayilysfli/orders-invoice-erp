package org.example.orderservice.repository;

import org.example.orderservice.model.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
    List<OrderEvent> findTop50ByProcessedFalseOrderByCreatedAtAsc();
}

