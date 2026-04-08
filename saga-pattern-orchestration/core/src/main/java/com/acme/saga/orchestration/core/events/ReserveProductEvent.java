package com.acme.saga.orchestration.core.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * This class represents a command sent from the order service to publish a new product event.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveProductEvent {

    private UUID orderId;
    private UUID productId;
    private int quantity;
}
