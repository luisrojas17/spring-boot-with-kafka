package com.appsdeveloperblog.products.web.controller;

import com.acme.saga.orchestration.core.dto.Product;
import com.appsdeveloperblog.products.dto.ProductCreationRequest;
import com.appsdeveloperblog.products.dto.ProductCreationResponse;
import com.appsdeveloperblog.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductsController {
    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Product> findAll() {
        return productService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCreationResponse save(@RequestBody @Valid ProductCreationRequest request) {
        var product = new Product();
        BeanUtils.copyProperties(request, product);
        Product result = productService.save(product);

        var response = new ProductCreationResponse();
        BeanUtils.copyProperties(result, response);

        log.info("It was saved product: [{}].", response.getId());

        return response;
    }
}
