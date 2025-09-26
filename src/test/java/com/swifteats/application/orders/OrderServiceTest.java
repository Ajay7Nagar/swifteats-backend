package com.swifteats.application.orders;

import com.swifteats.domain.orders.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOrderRepository implements OrderRepository {
    Order lastSaved;
    @Override public Order save(Order order) { this.lastSaved = order; return order; }
    @Override public Optional<Order> findById(UUID orderId) { return Optional.ofNullable(lastSaved); }
}

class NoopPaymentPublisher implements PaymentPublisher {
    UUID last;
    @Override public void publishPaymentRequested(UUID orderId, BigDecimal amount, String currency) { last = orderId; }
}

public class OrderServiceTest {

    @Test
    void createOrder_calculatesTotalAndSetsInitialStatuses() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        NoopPaymentPublisher publisher = new NoopPaymentPublisher();
        OrderService service = new OrderService(repo, publisher);

        OrderItem item1 = new OrderItem(UUID.randomUUID(), "Pizza", 2, new BigDecimal("100.00"), new BigDecimal("200.00"));
        OrderItem item2 = new OrderItem(UUID.randomUUID(), "Soda", 1, new BigDecimal("50.00"), new BigDecimal("50.00"));

        Order order = service.createOrder(UUID.randomUUID(), UUID.randomUUID(), List.of(item1, item2), "INR");

        assertNotNull(order.getId());
        assertEquals(OrderStatus.created, order.getStatus());
        assertEquals(PaymentStatus.pending, order.getPaymentStatus());
        assertEquals(new BigDecimal("250.00"), order.getTotalAmount());
        assertEquals("INR", order.getCurrency());
        assertEquals(order.getId(), publisher.last);
    }

    @Test
    void applyTransition_validFlow_advancesState() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        NoopPaymentPublisher publisher = new NoopPaymentPublisher();
        OrderService service = new OrderService(repo, publisher);

        Order base = new Order(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OrderStatus.created, PaymentStatus.pending, List.of(), new BigDecimal("0.00"), "INR", java.time.OffsetDateTime.now(), 0);
        repo.save(base);

        Order confirmed = service.applyTransition(base.getId(), OrderAction.confirm);
        assertEquals(OrderStatus.confirmed, confirmed.getStatus());

        repo.save(confirmed);
        Order assigned = service.applyTransition(base.getId(), OrderAction.assign_driver);
        assertEquals(OrderStatus.driver_assigned, assigned.getStatus());
    }

    @Test
    void applyTransition_invalid_throwsConflict() {
        InMemoryOrderRepository repo = new InMemoryOrderRepository();
        NoopPaymentPublisher publisher = new NoopPaymentPublisher();
        OrderService service = new OrderService(repo, publisher);
        Order base = new Order(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OrderStatus.created, PaymentStatus.pending, List.of(), new BigDecimal("0.00"), "INR", java.time.OffsetDateTime.now(), 0);
        repo.save(base);
        assertThrows(IllegalStateException.class, () -> service.applyTransition(base.getId(), OrderAction.pick_up));
    }
}


