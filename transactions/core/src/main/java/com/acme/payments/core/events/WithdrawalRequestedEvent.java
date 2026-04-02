package com.acme.payments.core.events;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class WithdrawalRequestedEvent {

    private String senderId;
    private String recipientId;
    private BigDecimal amount;

    public WithdrawalRequestedEvent() {

    }

    public WithdrawalRequestedEvent(String senderId, String recipientId, BigDecimal amount) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.amount = amount;
    }

}