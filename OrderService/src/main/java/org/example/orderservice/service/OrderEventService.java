package org.example.orderservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.orderservice.model.EventType;
import org.example.orderservice.model.Order;
import org.example.orderservice.model.OrderEvent;
import org.example.orderservice.messaging.OrderEventPublisher;
import org.example.orderservice.repository.OrderEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderEventService {
    private static final Logger log = LoggerFactory.getLogger(OrderEventService.class);
    private final OrderEventRepository eventRepository;
    private final OrderEventPublisher publisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void recordOrderCreated(Order order) {
        try {
            Map<String,Object> payload = new HashMap<>();
            payload.put("orderId", order.getId());
            payload.put("orderNumber", order.getOrderNumber());
            payload.put("customerId", order.getCustomerId());
            payload.put("totalAmount", order.getTotalAmount());
            payload.put("createdAt", order.getOrderDate());
            OrderEvent evt = new OrderEvent();
            evt.setEventType(EventType.ORDER_CREATED);
            evt.setOrderId(order.getId());
            evt.setPayload(objectMapper.writeValueAsString(payload));
            eventRepository.save(evt);
            log.info("Recorded ORDER_CREATED event orderId={}", order.getId());
            publisher.publish("order.created", payload);
        } catch (Exception e) {
            log.error("Failed to record ORDER_CREATED event: {}", e.getMessage());
        }
    }

    public void recordInvoiceGenerated(Long orderId, String orderNumber, String pdfPath, BigDecimal total) {
        try {
            Map<String,Object> payload = new HashMap<>();
            payload.put("orderId", orderId);
            payload.put("orderNumber", orderNumber);
            payload.put("pdfPath", pdfPath);
            payload.put("totalAmount", total);
            payload.put("generatedAt", LocalDateTime.now());
            OrderEvent evt = new OrderEvent();
            evt.setEventType(EventType.INVOICE_GENERATED);
            evt.setOrderId(orderId);
            evt.setPayload(objectMapper.writeValueAsString(payload));
            eventRepository.save(evt);
            log.info("Recorded INVOICE_GENERATED event orderId={}", orderId);
            publisher.publish("invoice.generated", payload);
        } catch (Exception e) {
            log.error("Failed to record INVOICE_GENERATED event: {}", e.getMessage());
        }
    }
}
