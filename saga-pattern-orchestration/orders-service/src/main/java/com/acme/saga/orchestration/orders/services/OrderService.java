package com.acme.saga.orchestration.orders.services;

import com.acme.saga.orchestration.core.dto.Order;

public interface OrderService {
    Order placeOrder(Order order);
}
