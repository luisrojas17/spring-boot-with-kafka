package com.acme.saga.orchestration.core.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReservationFailedEvent {

    private UUID orderId;
    private UUID productId;
    private int quantity;
}
