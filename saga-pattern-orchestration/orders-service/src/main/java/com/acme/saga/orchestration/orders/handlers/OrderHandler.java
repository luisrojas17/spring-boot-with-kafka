package com.acme.saga.orchestration.orders.handlers;

import com.acme.saga.orchestration.core.events.ReserveProductEvent;
import com.acme.saga.orchestration.core.events.OrderCreatedEvent;
import com.acme.saga.orchestration.core.enums.OrderStatus;
import com.acme.saga.orchestration.orders.services.OrderHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * This class acts as a Kafka consumer to consume order events and produce ReserveProductEvent to
 * products events topic.
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

        ReserveProductEvent reserveProductEvent = ReserveProductEvent.builder()
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getProductQuantity())
                .orderId(orderCreatedEvent.getOrderId())
                .build();

        log.info("Producing reserve product event [{}, {}]",
                orderCreatedEvent.getOrderId(), orderCreatedEvent.getProductId());

        kafkaTemplate.send(productsEventsTopicName, reserveProductEvent);

        log.info("It was created receive product event: [{}, {}].",
                reserveProductEvent.getOrderId(), reserveProductEvent.getProductId());

        // To save order into history database
        orderHistoryService.add(orderCreatedEvent.getOrderId(), OrderStatus.CREATED);
    }
}
