package com.swifteats.adapters.messaging;

import com.swifteats.domain.orders.PaymentPublisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@Profile("docker")
public class RabbitPaymentPublisher implements PaymentPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public RabbitPaymentPublisher(RabbitTemplate rabbitTemplate,
                                  @Value("${payments.queue:payments.process}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueName = queueName;
    }

    @Override
    public void publishPaymentRequested(UUID orderId, BigDecimal amount, String currency) {
        rabbitTemplate.convertAndSend(queueName, Map.of(
                "orderId", orderId.toString(),
                "amount", amount,
                "currency", currency
        ));
    }
}
