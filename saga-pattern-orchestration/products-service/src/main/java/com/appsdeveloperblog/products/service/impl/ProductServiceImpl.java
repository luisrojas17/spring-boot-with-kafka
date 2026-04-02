package com.appsdeveloperblog.products.service.impl;

import com.acme.saga.orchestration.core.dto.Product;
import com.acme.saga.orchestration.core.exceptions.ProductInsufficientQuantityException;
import com.appsdeveloperblog.products.repositories.entities.ProductEntity;
import com.appsdeveloperblog.products.repositories.ProductRepository;
import com.appsdeveloperblog.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product reserve(Product desiredProduct, UUID orderId) {

        log.info("Checking if there are products [{}] available for order [{}].",
                desiredProduct.getId(), orderId);

        ProductEntity productEntity = productRepository.findById(desiredProduct.getId()).orElseThrow();

        if (desiredProduct.getQuantity() > productEntity.getQuantity()) {

            log.warn("There are not products [{}] available.", desiredProduct.getId());

            throw new ProductInsufficientQuantityException(productEntity.getId(), orderId);
        }

        log.info("Updating product's quantity for product id [{}].", productEntity.getId());

        // If there are products available it will be decrease the quantity desired and saved it in database.
        productEntity.setQuantity(productEntity.getQuantity() - desiredProduct.getQuantity());
        productRepository.save(productEntity);

        log.info("It was updated product entity [{}].", productEntity.getId());

        var reservedProduct = new Product();
        BeanUtils.copyProperties(productEntity, reservedProduct);
        reservedProduct.setQuantity(desiredProduct.getQuantity());

        log.info("It was received ");

        return reservedProduct;
    }

    @Override
    public void cancelReservation(Product productToCancel, UUID orderId) {
        ProductEntity productEntity = productRepository.findById(productToCancel.getId()).orElseThrow();
        productEntity.setQuantity(productEntity.getQuantity() + productToCancel.getQuantity());
        productRepository.save(productEntity);
    }

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setName(product.getName());
        productEntity.setPrice(product.getPrice());
        productEntity.setQuantity(product.getQuantity());
        productRepository.save(productEntity);

        return new Product(productEntity.getId(), product.getName(), product.getPrice(), product.getQuantity());
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll().stream()
                .map(entity -> new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getQuantity()))
                .collect(Collectors.toList());
    }
}
