package com.swifteats.adapters.messaging;

import com.swifteats.domain.orders.PaymentPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("!docker")
public class NoopPaymentPublisher implements PaymentPublisher {
    @Override
    public void publishPaymentRequested(UUID orderId, BigDecimal amount, String currency) {
        // No-op publisher placeholder
    }
}


