package com.swifteats.domain.orders;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentPublisher {
    void publishPaymentRequested(UUID orderId, BigDecimal amount, String currency);
}


