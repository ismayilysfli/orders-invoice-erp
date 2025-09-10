package org.example.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EVENTS_EXCHANGE = "orders.events";
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String INVOICE_GENERATED_QUEUE = "invoice.generated.queue";
    public static final String DLQ = "orders.dlq";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String INVOICE_GENERATED_ROUTING_KEY = "invoice.generated";

    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange(ORDER_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue invoiceGeneratedQueue() {
        return QueueBuilder.durable(INVOICE_GENERATED_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue dlq() {
        return new Queue(DLQ, true);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderEventsExchange)
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding invoiceGeneratedBinding(Queue invoiceGeneratedQueue, TopicExchange orderEventsExchange) {
        return BindingBuilder.bind(invoiceGeneratedQueue)
                .to(orderEventsExchange)
                .with(INVOICE_GENERATED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter producerMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerMessageConverter());
        return rabbitTemplate;
    }
}