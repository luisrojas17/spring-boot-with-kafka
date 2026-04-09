package com.acme.saga.orchestration.orders.web.controller;

import com.acme.saga.orchestration.core.dto.Order;
import com.acme.saga.orchestration.orders.dto.CreateOrderRequest;
import com.acme.saga.orchestration.orders.dto.CreateOrderResponse;
import com.acme.saga.orchestration.orders.dto.OrderHistoryResponse;
import com.acme.saga.orchestration.orders.services.OrderHistoryService;
import com.acme.saga.orchestration.orders.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;

    private final OrderHistoryService orderHistoryService;

    public OrdersController(OrderService orderService, OrderHistoryService orderHistoryService) {
        this.orderService = orderService;
        this.orderHistoryService = orderHistoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CreateOrderResponse create(@RequestBody @Valid CreateOrderRequest request) {
        var order = new Order();
        BeanUtils.copyProperties(request, order);
        Order createdOrder = orderService.create(order);

        var response = new CreateOrderResponse();
        BeanUtils.copyProperties(createdOrder, response);
        return response;
    }

    @GetMapping("/{orderId}/history")
    @ResponseStatus(HttpStatus.OK)
    public List<OrderHistoryResponse> getOrderHistory(@PathVariable UUID orderId) {
        return orderHistoryService.findById(orderId).stream().map(orderHistory -> {
            OrderHistoryResponse orderHistoryResponse = new OrderHistoryResponse();
            BeanUtils.copyProperties(orderHistory, orderHistoryResponse);
            return orderHistoryResponse;
        }).toList();
    }
}
