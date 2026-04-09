package com.acme.saga.orchestration.products.handlers;

import com.acme.saga.orchestration.core.events.ProductReservationFailedEvent;
import com.acme.saga.orchestration.core.events.ReserveProductEvent;
import com.acme.saga.orchestration.core.dto.Product;
import com.acme.saga.orchestration.core.events.ProductReservedEvent;
import com.acme.saga.orchestration.products.services.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * This class acts as a Kafka Consumer to consume reserve products events and produce
 * ProductReservedEvent to products events topic.
 */
@Slf4j
@Component
@KafkaListener(topics = "${products.events.topic.name}")
public class ProductHandler {

    private final ProductService productService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String productsEventsTopicName;

    public ProductHandler(final ProductService productService,
                          final KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${products.events.topic.name}")
                          final String productsEventsTopicName) {

        this.productService = productService;
        this.kafkaTemplate = kafkaTemplate;
        this.productsEventsTopicName = productsEventsTopicName;
    }

    @KafkaHandler
    public void handler(ReserveProductEvent event) {

        try {

            log.info("Receiving reserve product event: [{}].", event);

            // To reserve a product
            Product product =
                    Product.builder()
                            .id(event.getProductId())
                            .quantity(event.getQuantity())
                            .build();

            product = productService.reserve(product, event.getOrderId());

            ProductReservedEvent productReservedEvent =
                    ProductReservedEvent.builder()
                            .orderId(event.getOrderId())
                            .productId(event.getProductId())
                            .price(product.getPrice())
                            .quantity(event.getQuantity())
                            .build();

            log.info("Producing product reserved event [{}, {}]",
                    event.getOrderId(), event.getProductId());

            kafkaTemplate.send(productsEventsTopicName, productReservedEvent);

            log.info("It was created product reserved event [{}, {}]",
                    productReservedEvent.getOrderId(), productReservedEvent.getProductId());

        } catch (Exception e) {
            log.error("Error reserving product: [{}].", event, e);

            // If product reservation fails it will be produce a new event to register the product.
            ProductReservationFailedEvent productReservationFailedEvent =
                    ProductReservationFailedEvent.builder()
                            .orderId(event.getOrderId())
                            .productId(event.getProductId())
                            .quantity(event.getQuantity())
                            .build();

            log.warn("Producing product reservation failed event [{}, {}]",
                    productReservationFailedEvent.getOrderId(), productReservationFailedEvent.getProductId());

            kafkaTemplate.send(productsEventsTopicName, productReservationFailedEvent);

            log.warn("It was created product reservation failed event [{}, {}]",
                    productReservationFailedEvent.getOrderId(), productReservationFailedEvent.getProductId());
        }

    }

}
