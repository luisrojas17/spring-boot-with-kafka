package com.appsdeveloperblog.products.handlers;

import com.acme.saga.orchestration.core.commands.ReserveProductCommand;
import com.acme.saga.orchestration.core.dto.Product;
import com.appsdeveloperblog.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = "${products.events.topic.name}")
public class ProductHandler {

    private final ProductService productService;

    public ProductHandler(ProductService productService) {
        this.productService = productService;
    }

    @KafkaHandler
    public void handler(ReserveProductCommand reserveProductCommand) {

        try {

            log.info("Receiving product event: [{}].", reserveProductCommand);

            // To reserve a product
            Product product =
                    new Product(reserveProductCommand.getProductId(), reserveProductCommand.getQuantity());

            productService.reserve(product, reserveProductCommand.getOrderId());

        } catch (Exception e) {
            log.error("Error reserving product: [{}].", reserveProductCommand, e);
        }

    }

}
