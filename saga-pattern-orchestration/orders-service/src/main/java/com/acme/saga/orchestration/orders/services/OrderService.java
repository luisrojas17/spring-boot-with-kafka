package com.acme.saga.orchestration.orders.services;

import com.acme.saga.orchestration.core.dto.Order;

import java.util.UUID;

public interface OrderService {
    Order create(Order order);
    void approve(UUID orderId);
}
