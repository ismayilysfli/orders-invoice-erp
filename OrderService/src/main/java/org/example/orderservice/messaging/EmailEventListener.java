package org.example.orderservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailEventListener {
    private static final Logger log = LoggerFactory.getLogger(EmailEventListener.class);

    @RabbitListener(queues = RabbitConfig.QUEUE_EMAIL)
    public void handleEvent(Map<String,Object> payload, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey){
        try {
            Object orderId = payload.get("orderId");
            log.info("[EMAIL] Simulated email sent routingKey={} orderId={} payloadKeys={}", routingKey, orderId, payload.keySet());
        } catch (Exception e){
            log.warn("[EMAIL] Failed processing event routingKey={} error={}", routingKey, e.getMessage());
            throw e; // let container handle retry / DLQ
        }
    }
}

