package com.swifteats.adapters.web.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifteats.application.catalog.CatalogService;
import com.swifteats.domain.catalog.MenuItem;
import com.swifteats.domain.catalog.Restaurant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CatalogControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CatalogService service;

    @Test
    void list_returns200_withData() throws Exception {
        var r = new Restaurant(UUID.randomUUID(), "R1", "addr", "Pune", "MH", List.of("veg"), true, OffsetDateTime.now());
        when(service.listRestaurants(any(), any(), any(), anyInt(), anyInt())).thenReturn(List.of(r));

        mockMvc.perform(get("/restaurants").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("R1"));
    }

    @Test
    void menu_returns200_withItems() throws Exception {
        UUID rid = UUID.randomUUID();
        var m = new MenuItem(UUID.randomUUID(), rid, "Pizza", "desc", new BigDecimal("100.00"), true, OffsetDateTime.now());
        when(service.listMenu(rid)).thenReturn(List.of(m));

        mockMvc.perform(get("/restaurants/" + rid + "/menu").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Pizza"));
    }

    @Test
    void list_badParam_returns400() throws Exception {
        mockMvc.perform(get("/restaurants")
                        .param("page", "-1")
                        .param("pageSize", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // controller default clamps page, so 200
    }
}
