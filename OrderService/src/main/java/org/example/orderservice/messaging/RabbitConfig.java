package org.example.orderservice.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@Configuration
@EnableRabbit
public class RabbitConfig {
    public static final String EXCHANGE = "orders.events";
    public static final String QUEUE_EMAIL = "orders.events.email";
    public static final String QUEUE_DLQ = "orders.dlq";

    @Bean
    public TopicExchange ordersExchange() { return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build(); }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() { return QueueBuilder.durable(QUEUE_DLQ).build(); }

    @Bean
    public Binding bindingOrderCreated() { return BindingBuilder.bind(emailQueue()).to(ordersExchange()).with("order.created"); }

    @Bean
    public Binding bindingInvoiceGenerated() { return BindingBuilder.bind(emailQueue()).to(ordersExchange()).with("invoice.generated"); }

    @Bean
    public Jackson2JsonMessageConverter jacksonMessageConverter(){ return new Jackson2JsonMessageConverter(); }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv){
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(conv);
        return tpl;
    }
}
