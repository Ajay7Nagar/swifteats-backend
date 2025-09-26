package com.swifteats.adapters.web.catalog;

import com.swifteats.application.catalog.CatalogService;
import com.swifteats.domain.catalog.MenuItem;
import com.swifteats.domain.catalog.Restaurant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<RestaurantListResponse> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        List<Restaurant> data = service.listRestaurants(city, state, tags, page, pageSize);
        return ResponseEntity.ok(new RestaurantListResponse(data, page, pageSize, data.size()));
    }

    @GetMapping("/{restaurantId}/menu")
    public ResponseEntity<MenuResponse> menu(@PathVariable UUID restaurantId) {
        List<MenuItem> items = service.listMenu(restaurantId);
        return ResponseEntity.ok(new MenuResponse(restaurantId, items));
    }

    public record RestaurantListResponse(List<Restaurant> data, int page, int pageSize, int total) {}
    public record MenuResponse(UUID restaurantId, List<MenuItem> items) {}
}


