package com.swifteats.adapters.web.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifteats.application.orders.OrderService;
import com.swifteats.domain.orders.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderService orderService;

    @Test
    void create_returns201_withOrderIdAndStatus() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order mock = new Order(orderId, UUID.randomUUID(), UUID.randomUUID(), OrderStatus.created, PaymentStatus.pending,
                List.of(), new BigDecimal("0.00"), "INR", OffsetDateTime.now(), 0);
        when(orderService.createOrder(any(), any(), any(), any())).thenReturn(mock);

        var req = new OrderController.OrderCreateRequest(
                UUID.randomUUID(), UUID.randomUUID(), "INR",
                List.of(new OrderController.OrderItemRequest(UUID.randomUUID(), "Pizza", 1, new BigDecimal("100"), new BigDecimal("100")))
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("created"));
    }

    @Test
    void get_returns200_withOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        Order mock = new Order(orderId, UUID.randomUUID(), UUID.randomUUID(), OrderStatus.created, PaymentStatus.pending,
                List.of(), new BigDecimal("0.00"), "INR", OffsetDateTime.now(), 0);
        when(orderService.getOrder(orderId)).thenReturn(mock);

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void transition_valid_returns200() throws Exception {
        UUID oid = UUID.randomUUID();
        Order updated = new Order(oid, UUID.randomUUID(), UUID.randomUUID(), OrderStatus.confirmed, PaymentStatus.pending, List.of(), new BigDecimal("0.00"), "INR", OffsetDateTime.now(), 0);
        when(orderService.applyTransition(eq(oid), eq(OrderAction.confirm))).thenReturn(updated);
        String body = "{\"action\":\"confirm\"}";
        mockMvc.perform(post("/orders/"+oid+"/transition").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("confirmed"));
    }

    @Test
    void transition_invalid_returns409() throws Exception {
        UUID oid = UUID.randomUUID();
        when(orderService.applyTransition(eq(oid), eq(OrderAction.pick_up))).thenThrow(new IllegalStateException("INVALID_TRANSITION"));
        String body = "{\"action\":\"pick_up\"}";
        mockMvc.perform(post("/orders/"+oid+"/transition").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void get_notFound_returns404_withErrorResponse() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(orderService.getOrder(orderId)).thenThrow(new java.util.NoSuchElementException("Order not found"));

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Order not found"))
                .andExpect(jsonPath("$.path").value("/orders/" + orderId));
    }

    @Test
    void transition_invalid_returns409_withErrorResponse() throws Exception {
        UUID oid = UUID.randomUUID();
        when(orderService.applyTransition(eq(oid), eq(OrderAction.pick_up))).thenThrow(new IllegalStateException("INVALID_TRANSITION"));
        String body = "{\"action\":\"pick_up\"}";

        mockMvc.perform(post("/orders/" + oid + "/transition").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("INVALID_TRANSITION"))
                .andExpect(jsonPath("$.path").value("/orders/" + oid + "/transition"));
    }

    @Test
    void create_invalid_returns400_withValidationErrors() throws Exception {
        // Missing required fields will trigger validation errors
        String badBody = "{\"customerId\":null,\"restaurantId\":null,\"currency\":\"\",\"items\":null}";

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}


