package com.appsdeveloperblog.payments.service;

import com.acme.saga.orchestration.core.dto.Payment;

import java.util.List;

public interface PaymentService {
    List<Payment> findAll();

    Payment process(Payment payment);
}
