package com.acme.saga.orchestration.payments.handlers;

import com.acme.saga.orchestration.core.dto.Payment;
import com.acme.saga.orchestration.core.events.PaymentFailedEvent;
import com.acme.saga.orchestration.core.events.PaymentProcessedEvent;
import com.acme.saga.orchestration.core.events.ProcessPaymentEvent;
import com.acme.saga.orchestration.core.exceptions.CreditCardProcessorUnavailableException;
import com.acme.saga.orchestration.payments.services.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * This class acts as a Kafka Consumer for Process Payment Events and produce
 * a Payment Processed Event to payment events topic.
 */
@Slf4j
@Component
@KafkaListener(topics = {"${payments.events.topic.name}"})
public class PaymentHandler {

    private final PaymentService paymentService;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String paymentsEventsTopicName;

    public PaymentHandler(final PaymentService paymentService,
                          final KafkaTemplate<String, Object> kafkaTemplate,
                          @Value("${payments.events.topic.name}")
                          final String paymentsEventsTopicName) {

        this.paymentService = paymentService;
        this.kafkaTemplate = kafkaTemplate;
        this.paymentsEventsTopicName = paymentsEventsTopicName;
    }

    /**
     * To handle the event to process payment which is produced by ProductHandler.
     * Also, it is produced a new PaymentProcessedEvent to payment events topic.
     *
     * @param event an instance of ProcessPaymentEvent.
     */
    @KafkaHandler
    public void handler(@Payload ProcessPaymentEvent event) {

        try {

            log.info("Receiving process payment event: [{}, {}]",
                    event.getOrderId(), event.getProductId());

            Payment payment = Payment.builder()
                    .orderId(event.getOrderId())
                    .productId(event.getProductId())
                    .productPrice(event.getPrice())
                    .productQuantity(event.getQuantity())
                    .build();

            Payment processedPayment = paymentService.process(payment);

            PaymentProcessedEvent paymentProcessedEvent =
                    PaymentProcessedEvent.builder()
                            .orderId(processedPayment.getOrderId())
                            .pymentId(processedPayment.getId())
                            .build();

            log.info("Producing payment processed event [{}, {}]",
                    paymentProcessedEvent.getOrderId(), paymentProcessedEvent.getPymentId());

            kafkaTemplate.send(paymentsEventsTopicName, paymentProcessedEvent);

            log.info("It was created payment processed event: [{}, {}].",
                    paymentProcessedEvent.getOrderId(), paymentProcessedEvent.getPymentId());

        } catch (CreditCardProcessorUnavailableException e) {
            log.error("Error to process payment.", e);

            PaymentFailedEvent paymentFailedEvent =
                    PaymentFailedEvent.builder()
                            .orderId(event.getOrderId())
                            .productId(event.getProductId())
                            .quantity(event.getQuantity())
                            .build();

            log.warn("Producing payment failed event [{}, {}]",
                    paymentFailedEvent.getOrderId(), paymentFailedEvent.getProductId());

            kafkaTemplate.send(paymentsEventsTopicName, paymentFailedEvent);

            log.warn("It was created payment failed event [{}, {}]",
                    paymentFailedEvent.getOrderId(), paymentFailedEvent.getProductId());
        }

    }
}
