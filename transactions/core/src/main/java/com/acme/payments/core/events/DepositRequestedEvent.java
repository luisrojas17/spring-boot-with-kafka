package com.acme.payments.core.events;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DepositRequestedEvent {
    
    private String senderId;
    private String recipientId;
    private BigDecimal amount;

    public DepositRequestedEvent() {

    }

    public DepositRequestedEvent(String senderId, String recipientId, BigDecimal amount) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.amount = amount;
    }

}