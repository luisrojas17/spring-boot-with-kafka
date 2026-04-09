package com.acme.saga.orchestration.orders.handlers;

import com.acme.saga.orchestration.core.events.ProcessPaymentEvent;
import com.acme.saga.orchestration.core.events.ProductReservedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * This class acts as a Kafka Consumer for Product Reserved Events and produce
 * a Process Payment Event to payment events topic.
 */
@Slf4j
@Component
@KafkaListener(topics = {"${products.events.topic.name}"})
public class ProductHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String paymentsEventsTopicName;

    public ProductHandler(final KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${payments.events.topic.name}")
                          final String paymentsEventsTopicName) {

        this.kafkaTemplate = kafkaTemplate;
        this.paymentsEventsTopicName = paymentsEventsTopicName;
    }

    @KafkaHandler
    public void handler(@Payload ProductReservedEvent event) {

        log.info("Receiving product reserved event: [{}]", event);

        ProcessPaymentEvent processPaymentEvent =
                ProcessPaymentEvent.builder()
                        .orderId(event.getOrderId())
                        .productId(event.getProductId())
                        .price(event.getPrice())
                        .quantity(event.getQuantity())
                        .build();

        log.info("Producing process payment event [{}, {}]",
                processPaymentEvent.getOrderId(), processPaymentEvent.getProductId());

        kafkaTemplate.send(paymentsEventsTopicName, processPaymentEvent);

        log.info("It was created process payment event [{}, {}]",
                processPaymentEvent.getOrderId(), processPaymentEvent.getProductId());
    }
}
