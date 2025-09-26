package com.swifteats.adapters.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Profile("docker")
public class PaymentWorker {

    @RabbitListener(queues = "${payments.queue:payments.process}")
    public void handle(Map<String, Object> message) {
        // Mock processing: no-op
    }
}


