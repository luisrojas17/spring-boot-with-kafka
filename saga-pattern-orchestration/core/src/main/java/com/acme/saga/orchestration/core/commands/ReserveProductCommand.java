package com.acme.saga.orchestration.core.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * This class represents a command sent from the order service to publish a new product event.
 */
@Getter
@Setter
@Builder
public class ReserveProductCommand {
    private UUID productId;
    private int quantity;
    private UUID orderId;
}
