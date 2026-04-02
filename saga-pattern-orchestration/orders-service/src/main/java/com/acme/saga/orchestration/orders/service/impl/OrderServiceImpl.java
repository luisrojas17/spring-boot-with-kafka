package com.acme.saga.orchestration.orders.service.impl;

import com.acme.saga.orchestration.core.dto.Order;
import com.acme.saga.orchestration.core.events.OrderCreatedEvent;
import com.acme.saga.orchestration.core.enums.OrderStatus;
import com.acme.saga.orchestration.orders.repositories.entities.OrderEntity;
import com.acme.saga.orchestration.orders.repositories.OrderRepository;
import com.acme.saga.orchestration.orders.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String ordersEventsTopicName;

    public OrderServiceImpl(final OrderRepository orderRepository,
                            final KafkaTemplate<String, Object> kafkaTemplate,
                            @Value("${orders.events.topic.name}")
                            final String ordersEventsTopicName) {

        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.ordersEventsTopicName = ordersEventsTopicName;
    }

    @Override
    public Order placeOrder(Order order) {

        OrderEntity entity = new OrderEntity();
        entity.setCustomerId(order.getCustomerId());
        entity.setProductId(order.getProductId());
        entity.setProductQuantity(order.getProductQuantity());
        entity.setStatus(OrderStatus.CREATED);
        orderRepository.save(entity);

        log.info("It was saved order entity: [{}-{}].",
                entity.getId(), entity.getProductId());

        // To produce a new order created event.
        // We create the event in order to send it to Kafka topic
        OrderCreatedEvent orderCreatedEvent =
                OrderCreatedEvent.builder()
                        .orderId(entity.getId())
                        .customerId(entity.getCustomerId())
                        .productId(entity.getProductId())
                        .productQuantity(entity.getProductQuantity())
                        .build();

        kafkaTemplate.send(ordersEventsTopicName, orderCreatedEvent);

        log.info("It was created order event: [{}, {}].",
                orderCreatedEvent.getOrderId(), orderCreatedEvent.getProductId());

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getProductId(),
                entity.getProductQuantity(),
                entity.getStatus());
    }

}
