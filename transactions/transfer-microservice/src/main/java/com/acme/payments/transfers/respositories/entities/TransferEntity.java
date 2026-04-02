package com.acme.payments.transfers.respositories.entities;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="transfers")
public class TransferEntity implements Serializable {

    private static final long serialVersionUID = -6202121587624962351L;

	@Id
    @Column(nullable = false)
    private String transferId;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    private String recipientId;

    @Column(nullable = false)
    private BigDecimal amount;

    public TransferEntity() {
    }
 
    public TransferEntity(String transferId, String senderId, String recipientId, BigDecimal amount) {
        this.transferId = transferId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.amount = amount;
    }

}

