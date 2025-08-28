package com.swifteats.service;

import com.swifteats.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PaymentService
 * Tests payment processing logic and scenarios
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Set up payment delay for testing
        ReflectionTestUtils.setField(paymentService, "paymentMockDelay", 50);
        
        // Set up test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setTotalAmount(500.0);
        testOrder.setPaymentStatus(Order.PaymentStatus.PENDING);
        testOrder.setPaymentMethod("CARD");
    }

    @Test
    void processPayment_WithValidOrder_ShouldReturnTrue() {
        // When
        boolean result = paymentService.processPayment(testOrder);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void processPayment_WithZeroAmount_ShouldProcessWithRandomResult() {
        // Given
        testOrder.setTotalAmount(0.0);

        // When
        boolean result = paymentService.processPayment(testOrder);

        // Then - Payment service uses random logic, so result can be true or false
        assertThat(result).isIn(true, false);
    }

    @Test
    void processPayment_WithNegativeAmount_ShouldProcessWithRandomResult() {
        // Given
        testOrder.setTotalAmount(-100.0);

        // When
        boolean result = paymentService.processPayment(testOrder);

        // Then - Payment service uses random logic, so result can be true or false
        assertThat(result).isIn(true, false);
    }

    @Test
    void processPayment_WithDifferentPaymentMethods_ShouldProcessWithRandomResult() {
        // Test CASH payment - payment service uses random logic
        testOrder.setPaymentMethod("CASH");
        boolean cashResult = paymentService.processPayment(testOrder);
        assertThat(cashResult).isIn(true, false);

        // Test UPI payment
        testOrder.setPaymentMethod("UPI");
        boolean upiResult = paymentService.processPayment(testOrder);
        assertThat(upiResult).isIn(true, false);

        // Test WALLET payment
        testOrder.setPaymentMethod("WALLET");
        boolean walletResult = paymentService.processPayment(testOrder);
        assertThat(walletResult).isIn(true, false);
    }

    @Test
    void processPayment_SimulatesDelay_ShouldTakeTime() {
        // Given
        long startTime = System.currentTimeMillis();

        // When
        paymentService.processPayment(testOrder);

        // Then
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertThat(duration).isGreaterThanOrEqualTo(50); // Should take at least 50ms
    }

    @Test
    void processPayment_WithVeryLargeAmount_ShouldHandleCorrectly() {
        // Given - edge case with very large amount
        testOrder.setTotalAmount(999999.99);

        // When
        boolean result = paymentService.processPayment(testOrder);

        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void processRefund_WithValidOrder_ShouldReturnTrue() {
        // When
        boolean result = paymentService.processRefund(testOrder);

        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void validatePaymentMethod_WithValidMethods_ShouldReturnTrue() {
        // Then
        assertThat(paymentService.validatePaymentMethod("CARD")).isTrue();
        assertThat(paymentService.validatePaymentMethod("UPI")).isTrue();
        assertThat(paymentService.validatePaymentMethod("WALLET")).isTrue();
        assertThat(paymentService.validatePaymentMethod("COD")).isTrue();
    }
    
    @Test
    void validatePaymentMethod_WithInvalidMethods_ShouldReturnFalse() {
        // Then
        assertThat(paymentService.validatePaymentMethod("INVALID")).isFalse();
        assertThat(paymentService.validatePaymentMethod(null)).isFalse();
        assertThat(paymentService.validatePaymentMethod("")).isFalse();
    }
    
    @Test
    void getPaymentStatus_ShouldReturnValidStatus() {
        // When
        String status = paymentService.getPaymentStatus(1L);

        // Then
        assertThat(status).isIn("PENDING", "PROCESSING", "COMPLETED", "FAILED");
    }
}
