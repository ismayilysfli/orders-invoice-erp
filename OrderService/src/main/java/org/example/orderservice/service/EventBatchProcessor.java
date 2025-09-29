package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.model.OrderEvent;
import org.example.orderservice.repository.OrderEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class EventBatchProcessor {
    private static final Logger log = LoggerFactory.getLogger(EventBatchProcessor.class);
    private final OrderEventRepository repository;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Scheduled(fixedDelayString = "30000", initialDelay = 15000)
    public void processPendingEvents() {
        List<OrderEvent> events = repository.findTop50ByProcessedFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) return;
        log.info("Batch start size={}", events.size());
        for (OrderEvent ev : events) {
            executor.submit(() -> handleEvent(ev));
        }
    }

    private void handleEvent(OrderEvent ev) {
        try {
            // Simulate email sending or downstream publication
            log.info("Processing event id={} type={} orderId={} sending email notification", ev.getId(), ev.getEventType(), ev.getOrderId());
            // simulate some work
            TimeUnit.MILLISECONDS.sleep(100);
            ev.setProcessed(true);
            ev.setProcessedAt(LocalDateTime.now());
            repository.save(ev);
            log.info("Processed event id={} type={} status=OK", ev.getId(), ev.getEventType());
        } catch (Exception ex) {
            log.warn("Event processing failed id={} type={} error={}", ev.getId(), ev.getEventType(), ex.getMessage());
        }
    }
}

