package com.swifteats.service;

import com.swifteats.model.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Mock Payment Service for processing payments
 * Simulates payment gateway interactions with configurable delay
 */
@Service
public class PaymentService {
    
    private final Random random = new Random();
    
    @Value("${swifteats.order.payment-mock-delay:100}")
    private int paymentMockDelay;
    
    /**
     * Process payment for an order (mocked implementation)
     * 
     * @param order The order to process payment for
     * @return true if payment is successful, false otherwise
     */
    public boolean processPayment(Order order) {
        try {
            // Simulate payment processing delay
            Thread.sleep(paymentMockDelay);
            
            // Mock payment logic - 95% success rate for testing
            // In real implementation, this would call external payment gateway
            double successProbability = 0.95;
            boolean paymentSuccess = random.nextDouble() < successProbability;
            
            if (paymentSuccess) {
                // Log successful payment
                System.out.println("Payment processed successfully for order: " + order.getId() + 
                                 ", Amount: ₹" + order.getTotalAmount());
                
                // In real implementation, would:
                // 1. Call payment gateway API
                // 2. Store transaction ID
                // 3. Handle payment confirmations
                // 4. Implement retry logic for failures
                // 5. Handle refunds and disputes
                
                return true;
            } else {
                // Log payment failure
                System.out.println("Payment failed for order: " + order.getId() + 
                                 ", Amount: ₹" + order.getTotalAmount());
                return false;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Payment processing interrupted for order: " + order.getId());
            return false;
        } catch (Exception e) {
            System.err.println("Payment processing error for order: " + order.getId() + 
                             ", Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initiate refund for an order (mocked implementation)
     * 
     * @param order The order to refund
     * @return true if refund is successful, false otherwise
     */
    public boolean processRefund(Order order) {
        try {
            // Simulate refund processing delay
            Thread.sleep(paymentMockDelay);
            
            // Mock refund logic - 98% success rate
            double successProbability = 0.98;
            boolean refundSuccess = random.nextDouble() < successProbability;
            
            if (refundSuccess) {
                System.out.println("Refund processed successfully for order: " + order.getId() + 
                                 ", Amount: ₹" + order.getTotalAmount());
                return true;
            } else {
                System.out.println("Refund failed for order: " + order.getId());
                return false;
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Refund processing interrupted for order: " + order.getId());
            return false;
        } catch (Exception e) {
            System.err.println("Refund processing error for order: " + order.getId() + 
                             ", Error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate payment details (mocked implementation)
     * 
     * @param paymentMethod The payment method to validate
     * @return true if payment method is valid, false otherwise
     */
    public boolean validatePaymentMethod(String paymentMethod) {
        // Mock validation logic
        return paymentMethod != null && 
               (paymentMethod.equalsIgnoreCase("CARD") || 
                paymentMethod.equalsIgnoreCase("UPI") || 
                paymentMethod.equalsIgnoreCase("WALLET") ||
                paymentMethod.equalsIgnoreCase("COD"));
    }
    
    /**
     * Get payment status for an order (mocked implementation)
     * 
     * @param orderId The order ID to check payment status for
     * @return Payment status as string
     */
    public String getPaymentStatus(Long orderId) {
        // In real implementation, would query payment gateway
        // Mock implementation returns random status for demonstration
        String[] statuses = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"};
        return statuses[random.nextInt(statuses.length)];
    }
}
