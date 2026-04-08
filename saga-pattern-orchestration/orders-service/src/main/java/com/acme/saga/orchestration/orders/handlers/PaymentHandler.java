package com.acme.saga.orchestration.orders.handlers;

import com.acme.saga.orchestration.core.events.ApprovedOrderEvent;
import com.acme.saga.orchestration.core.events.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * This class acts as a Kafka Consumer for Payment Processed Event and produce
 * an Approved Order Event to order events topic.
 */
@Slf4j
@Component
@KafkaListener(topics = "payments.events.topic.name")
public class PaymentHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String ordersEventsTopicName;

    public PaymentHandler(final KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${orders.events.topic.name}")
                          final String ordersEventsTopicName) {

        this.kafkaTemplate = kafkaTemplate;
        this.ordersEventsTopicName = ordersEventsTopicName;

    }

    @KafkaHandler
    public void handler(@Payload PaymentProcessedEvent event) {

        log.info("Receiving payment processed event: [{}].", event);

        ApprovedOrderEvent approvedOrderEvent =
                ApprovedOrderEvent.builder()
                        .orderId(event.getOrderId())
                        .build();

        log.info("Producing approved order event: [{}, {}].",
                event.getOrderId(), event.getPymentId());

        kafkaTemplate.send(ordersEventsTopicName, approvedOrderEvent);

        log.info("It was created approved order event: [{}].",
                approvedOrderEvent.getOrderId());
    }
}
