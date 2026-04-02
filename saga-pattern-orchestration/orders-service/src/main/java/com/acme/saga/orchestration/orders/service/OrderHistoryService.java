package com.acme.saga.orchestration.orders.service;

import com.acme.saga.orchestration.core.enums.OrderStatus;
import com.acme.saga.orchestration.orders.dto.OrderHistory;

import java.util.List;
import java.util.UUID;

public interface OrderHistoryService {
    void add(UUID orderId, OrderStatus orderStatus);

    List<OrderHistory> findByOrderId(UUID orderId);
}
