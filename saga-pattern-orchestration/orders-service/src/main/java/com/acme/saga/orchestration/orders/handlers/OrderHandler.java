package com.acme.saga.orchestration.orders.handlers;

import com.acme.saga.orchestration.core.commands.ReserveProductCommand;
import com.acme.saga.orchestration.core.events.OrderCreatedEvent;
import com.acme.saga.orchestration.core.enums.OrderStatus;
import com.acme.saga.orchestration.orders.service.OrderHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * This class acts as a Kafka consumer to consume order events.
 *
 * @see org.springframework.kafka.annotation.KafkaListener
 * @see org.springframework.kafka.core.KafkaTemplate
 * @see org.springframework.kafka.annotation.KafkaHandler
 */
@Slf4j
@Component
@KafkaListener(topics = {"${orders.events.topic.name}"})
public class OrderHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String productsEventsTopicName;

    private final OrderHistoryService orderHistoryService;

    public OrderHandler(final KafkaTemplate<String, Object> kafkaTemplate,
                        @Value("${products.events.topic.name}")
                        final String productsEventsTopicName,
                        final OrderHistoryService orderHistoryService) {

        this.kafkaTemplate = kafkaTemplate;
        this.productsEventsTopicName = productsEventsTopicName;
        this.orderHistoryService = orderHistoryService;
    }

    @KafkaHandler
    public void handler(@Payload OrderCreatedEvent orderCreatedEvent) {

        log.info("Receiving order event: [{}].", orderCreatedEvent.getOrderId());

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .productId(orderCreatedEvent.getProductId().toString())
                .quantity(orderCreatedEvent.getProductQuantity())
                .orderId(orderCreatedEvent.getOrderId())
                .build();

        kafkaTemplate.send(productsEventsTopicName, reserveProductCommand);

        log.info("It was created product event: [{}, {}].",
                reserveProductCommand.getOrderId(), reserveProductCommand.getProductId());

        // To save order into history database
        orderHistoryService.add(orderCreatedEvent.getOrderId(), OrderStatus.CREATED);
    }
}
