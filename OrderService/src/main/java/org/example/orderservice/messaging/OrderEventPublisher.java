package org.example.orderservice.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public void publish(String routingKey, Map<String,Object> payload){
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, payload);
            log.info("Published event routingKey={} payloadKeys={}", routingKey, payload.keySet());
        } catch (Exception e){
            log.warn("Failed to publish event routingKey={} error={}", routingKey, e.getMessage());
        }
    }
}

