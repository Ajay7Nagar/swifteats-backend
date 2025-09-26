package com.swifteats.adapters.web.drivertracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifteats.application.drivertracking.DriverTrackingService;
import com.swifteats.domain.drivertracking.DriverLocation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DriverControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    DriverTrackingService service;

    @Test
    void setStatus_returns200() throws Exception {
        UUID driverId = UUID.randomUUID();
        doNothing().when(service).setStatus(eq(driverId), anyBoolean());

        mockMvc.perform(post("/drivers/" + driverId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"online\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"));
    }

    @Test
    void location_returns202() throws Exception {
        UUID driverId = UUID.randomUUID();
        doNothing().when(service).ingestLocation(eq(driverId), any(), anyDouble(), anyDouble(), any(), any());
        String payload = "{\"orderId\":\"" + UUID.randomUUID() + "\",\"lat\":18.52,\"lng\":73.86,\"timestamp\":\"" + OffsetDateTime.now().toString() + "\"}";

        mockMvc.perform(post("/drivers/" + driverId + "/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.accepted").value(true));
    }

    @Test
    void latest_returns200_or204() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(service.getLatestForOrder(orderId)).thenReturn(Optional.of(new DriverLocation(UUID.randomUUID(), orderId, 18.52, 73.86, OffsetDateTime.now())));

        mockMvc.perform(get("/orders/" + orderId + "/driver-location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void location_rateLimited_returns429() throws Exception {
        UUID driverId = UUID.randomUUID();
        doNothing().when(service).setStatus(any(), anyBoolean());
        doNothing().when(service).setStatus(any(), anyBoolean());
        doThrow(new IllegalArgumentException("RATE_LIMITED"))
                .when(service).ingestLocation(eq(driverId), any(), anyDouble(), anyDouble(), any(), any());
        String payload = "{\"orderId\":\"" + UUID.randomUUID() + "\",\"lat\":18.52,\"lng\":73.86,\"timestamp\":\"" + OffsetDateTime.now().toString() + "\"}";

        mockMvc.perform(post("/drivers/" + driverId + "/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("RATE_LIMITED"));
    }
}


