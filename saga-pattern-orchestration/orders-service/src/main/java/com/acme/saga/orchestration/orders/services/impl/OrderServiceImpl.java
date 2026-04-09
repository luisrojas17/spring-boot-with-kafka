package com.acme.saga.orchestration.orders.services.impl;

import com.acme.saga.orchestration.core.dto.Order;
import com.acme.saga.orchestration.core.events.OrderApprovedEvent;
import com.acme.saga.orchestration.core.events.OrderCreatedEvent;
import com.acme.saga.orchestration.core.enums.OrderStatus;
import com.acme.saga.orchestration.orders.repositories.entities.OrderEntity;
import com.acme.saga.orchestration.orders.repositories.OrderRepository;
import com.acme.saga.orchestration.orders.services.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * This class contains business logic implementation to handle Orders.
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

    /**
     * To create an order into a database and produce an OrderCreatedEvent to orders events topic.
     *
     * @param order an instance of Order which represents the Order created into a database.
     *
     * @return an instance of Order which represents the Order created.
     */
    @Override
    public Order create(Order order) {

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

    /**
     * To approve the order and produce an OrderApprovedEvent to orders events topic.
     *
     * @param orderId the identifier of orderId.
     */
    public void approve(UUID orderId) {

        log.info("Approving [{}]...", orderId);

        OrderEntity orderEntity = orderRepository.findById(orderId).orElse(null);

        Assert.notNull(orderEntity, String.format("No order id [%s] was found into database.", orderId));
        orderEntity.setStatus(OrderStatus.APPROVED);
        orderRepository.save(orderEntity);

        log.info("It was updated the status for orderId [{}] into database.", orderId);

        OrderApprovedEvent event =
                OrderApprovedEvent.builder()
                        .orderId(orderId)
                        .build();

        kafkaTemplate.send(ordersEventsTopicName, event);

        log.info("It was approved the orderId [{}].", orderId);
    }

}
