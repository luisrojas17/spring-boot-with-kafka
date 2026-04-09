package com.acme.saga.orchestration.orders.handlers;

import com.acme.saga.orchestration.core.events.ApproveOrderEvent;
import com.acme.saga.orchestration.core.events.OrderApprovedEvent;
import com.acme.saga.orchestration.core.events.ReserveProductEvent;
import com.acme.saga.orchestration.core.events.OrderCreatedEvent;
import com.acme.saga.orchestration.core.enums.OrderStatus;
import com.acme.saga.orchestration.orders.services.OrderHistoryService;
import com.acme.saga.orchestration.orders.services.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * This class acts as a Kafka consumer to consume:
 * <ul>
 *     <li>OrderCreatedEvent and produced ReserveProductEvent to products events topic.</li>
 *     <li>ApproveOrderEvent and produce OrderApprovedEvent to orders events topic.</li>
 * </ul>
 *
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

    private final OrderService orderService;

    private final OrderHistoryService orderHistoryService;

    public OrderHandler(final KafkaTemplate<String, Object> kafkaTemplate,
                        @Value("${products.events.topic.name}")
                        final String productsEventsTopicName,
                        final OrderService orderService,
                        final OrderHistoryService orderHistoryService) {

        this.kafkaTemplate = kafkaTemplate;
        this.productsEventsTopicName = productsEventsTopicName;
        this.orderService = orderService;
        this.orderHistoryService = orderHistoryService;
    }

    /**
     * To handler the order created events which are published by OrderServiceImpl.
     * Also, it is produced a new ReserveProductEvent to products events topic.
     *
     * @param orderCreatedEvent an instance of OrderCreatedEvent.
     */
    @KafkaHandler
    public void handler(@Payload OrderCreatedEvent orderCreatedEvent) {

        log.info("Receiving order created event: [{}].", orderCreatedEvent.getOrderId());

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

    /**
     * To handler approve order events which are produced by PaymentHandler.
     *
     * @param event an instance of ApproveOrderEvent.
     */
    @KafkaHandler
    public void handler(@Payload ApproveOrderEvent event) {

        log.info("Receiving approve order event: [{}].", event.getOrderId());

        orderService.approve(event.getOrderId());
    }

    /**
     * To handle order approved events which are produced by OrderService.
     *
     * @param event an instance of OrderApprovedEvent.
     */
    @KafkaHandler
    public  void handler(@Payload OrderApprovedEvent event) {

        log.info("Receiving order approved event: [{}].", event.getOrderId());

        orderHistoryService.add(event.getOrderId(), OrderStatus.APPROVED);

    }
}
