package com.acme.saga.orchestration.orders.service.impl;

import com.acme.saga.orchestration.core.enums.OrderStatus;
import com.acme.saga.orchestration.orders.repositories.entities.OrderHistoryEntity;
import com.acme.saga.orchestration.orders.repositories.OrderHistoryRepository;
import com.acme.saga.orchestration.orders.dto.OrderHistory;
import com.acme.saga.orchestration.orders.service.OrderHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderHistoryServiceImpl implements OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;

    public OrderHistoryServiceImpl(OrderHistoryRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Override
    public void add(UUID orderId, OrderStatus orderStatus) {
        log.info("Saving order history [{}, {}] into database...", orderId, orderStatus);

        OrderHistoryEntity entity = new OrderHistoryEntity();
        entity.setOrderId(orderId);
        entity.setStatus(orderStatus);
        entity.setCreatedAt(new Timestamp(new Date().getTime()));
        orderHistoryRepository.save(entity);

        log.info("It was saved order history [{}, {}] into database.",
                entity.getId(), entity.getOrderId());
    }

    @Override
    public List<OrderHistory> findByOrderId(UUID orderId) {
        var entities = orderHistoryRepository.findByOrderId(orderId);
        return entities.stream().map(entity -> {
            OrderHistory orderHistory = new OrderHistory();
            BeanUtils.copyProperties(entity, orderHistory);
            return orderHistory;
        }).toList();
    }
}
