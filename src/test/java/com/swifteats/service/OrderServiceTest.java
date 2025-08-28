package com.swifteats.service;

import com.swifteats.dto.CreateOrderRequestDto;
import com.swifteats.dto.OrderResponseDto;
import com.swifteats.model.*;
import com.swifteats.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService
 * Tests high-volume order processing and business logic
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private MenuItemRepository menuItemRepository;
    
    @Mock
    private DriverRepository driverRepository;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private DriverAssignmentService driverAssignmentService;
    
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @InjectMocks
    private OrderService orderService;
    
    private Customer testCustomer;
    private Restaurant testRestaurant;
    private MenuItem testMenuItem;
    private Driver testDriver;
    private Order testOrder;
    private CreateOrderRequestDto createOrderRequest;
    
    @BeforeEach
    void setUp() {
        // Set up test data
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Test Customer");
        testCustomer.setEmail("test@customer.com");
        
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setStatus(Restaurant.RestaurantStatus.ACTIVE);
        testRestaurant.setDeliveryFee(50.0);
        testRestaurant.setAverageDeliveryTime(30);
        testRestaurant.setLatitude(19.0760);
        testRestaurant.setLongitude(72.8777);
        
        testMenuItem = new MenuItem();
        testMenuItem.setId(1L);
        testMenuItem.setName("Test Item");
        testMenuItem.setPrice(299.0);
        testMenuItem.setAvailable(true);
        testMenuItem.setRestaurant(testRestaurant);
        
        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setName("Test Driver");
        testDriver.setStatus(Driver.DriverStatus.ONLINE);
        
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomer(testCustomer);
        testOrder.setRestaurant(testRestaurant);
        testOrder.setTotalAmount(348.0); // Item price + delivery fee + tax
        testOrder.setDeliveryFee(50.0); // Set delivery fee to avoid null pointer
        testOrder.setStatus(Order.OrderStatus.PLACED);
        testOrder.setPaymentStatus(Order.PaymentStatus.PENDING);
        
        // Set up create order request
        createOrderRequest = new CreateOrderRequestDto();
        createOrderRequest.setCustomerId(1L);
        createOrderRequest.setRestaurantId(1L);
        createOrderRequest.setDeliveryAddress("123 Test Street");
        createOrderRequest.setPaymentMethod("CARD");
        
        CreateOrderRequestDto.OrderItemRequestDto orderItemRequest = new CreateOrderRequestDto.OrderItemRequestDto();
        orderItemRequest.setMenuItemId(1L);
        orderItemRequest.setQuantity(1);
        createOrderRequest.setOrderItems(Arrays.asList(orderItemRequest));
        
        // Set up @Value fields that aren't injected in unit tests
        ReflectionTestUtils.setField(orderService, "maxOrdersPerMinute", 500);
    }
    
    @Test
    void createOrder_WithValidData_ShouldCreateOrderSuccessfully() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderRepository.countOrdersInLastHour(any(LocalDateTime.class))).thenReturn(10L);
        
        // When
        OrderResponseDto result = orderService.createOrder(createOrderRequest);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(1L);
        assertThat(result.getRestaurantId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("PLACED");
        
        verify(orderRepository, times(2)).save(any(Order.class)); // Once for initial save, once for final save
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString()); // Async, so not called immediately
    }
    
    @Test
    void createOrder_WithNonExistentCustomer_ShouldThrowException() {
        // Given
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Customer not found");
        
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    void createOrder_WithInactiveRestaurant_ShouldThrowException() {
        // Given
        testRestaurant.setStatus(Restaurant.RestaurantStatus.CLOSED);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        
        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Restaurant is not available");
        
        verify(orderRepository, never()).save(any());
    }
    
    @Test
    void createOrder_WithUnavailableMenuItem_ShouldThrowException() {
        // Given
        testMenuItem.setAvailable(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(orderRepository.countOrdersInLastHour(any(LocalDateTime.class))).thenReturn(10L);
        
        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Menu item is not available");
        
        verify(orderRepository, atMost(1)).save(any()); // Only initial save, not final save
    }
    
    @Test
    void createOrder_WithMenuItemFromDifferentRestaurant_ShouldThrowException() {
        // Given
        Restaurant differentRestaurant = new Restaurant();
        differentRestaurant.setId(2L);
        testMenuItem.setRestaurant(differentRestaurant);
        
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(orderRepository.countOrdersInLastHour(any(LocalDateTime.class))).thenReturn(10L);
        
        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Menu item does not belong to the specified restaurant");
    }
    
    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        
        // When
        OrderResponseDto result = orderService.getOrderById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(1L);
        
        verify(orderRepository).findById(1L);
    }
    
    @Test
    void getOrderById_WhenOrderNotExists_ShouldThrowException() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Order not found");
        
        verify(orderRepository).findById(999L);
    }
    
    @Test
    void updateOrderStatus_WithValidTransition_ShouldUpdateSuccessfully() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // When
        OrderResponseDto result = orderService.updateOrderStatus(1L, Order.OrderStatus.CONFIRMED);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(kafkaTemplate).send(eq("order-events"), eq("status-updated"), anyString());
    }
    
    @Test
    void updateOrderStatus_WhenOrderReadyForPickup_ShouldTriggerDriverAssignment() {
        // Given
        testOrder.setStatus(Order.OrderStatus.PREPARING); // Set correct initial status for transition
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        
        // When
        orderService.updateOrderStatus(1L, Order.OrderStatus.READY_FOR_PICKUP);
        
        // Then
        verify(driverAssignmentService).assignDriverToOrder(any(Order.class));
    }
    
    @Test
    void updateOrderStatus_WhenOrderDelivered_ShouldSetDeliveryTime() {
        // Given
        testOrder.setStatus(Order.OrderStatus.OUT_FOR_DELIVERY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            assertThat(savedOrder.getActualDeliveryTime()).isNotNull();
            return savedOrder;
        });
        
        // When
        orderService.updateOrderStatus(1L, Order.OrderStatus.DELIVERED);
        
        // Then
        verify(orderRepository).save(any(Order.class));
    }
    
    @Test
    void assignDriver_WithAvailableDriver_ShouldAssignSuccessfully() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);
        
        // When
        OrderResponseDto result = orderService.assignDriver(1L, 1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDriverId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo("PICKED_UP");
        
        verify(orderRepository).save(any(Order.class));
        verify(driverRepository).save(any(Driver.class));
        verify(kafkaTemplate).send(eq("order-events"), eq("driver-assigned"), anyString());
    }
    
    @Test
    void assignDriver_WithUnavailableDriver_ShouldThrowException() {
        // Given
        testDriver.setStatus(Driver.DriverStatus.OFFLINE);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(testDriver));
        
        // When/Then
        assertThatThrownBy(() -> orderService.assignDriver(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver is not available");
        
        verify(orderRepository, never()).save(any(Order.class));
        verify(driverRepository, never()).save(any(Driver.class));
    }
    
    @Test
    void assignDriver_WithNonExistentDriver_ShouldThrowException() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(driverRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> orderService.assignDriver(1L, 999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Driver not found");
    }
    
    @Test
    void getOrdersByStatus_ShouldReturnOrdersWithSpecificStatus() {
        // Given
        when(orderRepository.findByStatus(Order.OrderStatus.PLACED))
                .thenReturn(Arrays.asList(testOrder));
        
        // When
        var result = orderService.getOrdersByStatus(Order.OrderStatus.PLACED);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PLACED");
        
        verify(orderRepository).findByStatus(Order.OrderStatus.PLACED);
    }
}
