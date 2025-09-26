package com.swifteats.adapters.web.drivertracking;

import com.swifteats.application.drivertracking.DriverTrackingService;
import com.swifteats.domain.drivertracking.DriverLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class DriverController {
    private final DriverTrackingService service;

    public DriverController(DriverTrackingService service) {
        this.service = service;
    }

    @PostMapping("/drivers/{driverId}/status")
    public ResponseEntity<Map<String, Object>> setStatus(@PathVariable UUID driverId, @RequestBody Map<String, String> req) {
        boolean online = "online".equalsIgnoreCase(req.getOrDefault("status", "offline"));
        service.setStatus(driverId, online);
        return ResponseEntity.ok(Map.of("driverId", driverId, "status", online ? "online" : "offline"));
    }

    @PostMapping("/drivers/{driverId}/location")
    public ResponseEntity<Map<String, Object>> location(@PathVariable UUID driverId, @Valid @RequestBody LocationRequest body) {
        service.ingestLocation(driverId, body.orderId(), body.lat(), body.lng(), body.timestamp(), body.lastSeenTimestamp());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("accepted", true, "receivedAt", OffsetDateTime.now().toString()));
    }

    @GetMapping("/orders/{orderId}/driver-location")
    public ResponseEntity<?> latest(@PathVariable UUID orderId) {
        Optional<DriverLocation> loc = service.getLatestForOrder(orderId);
        if (loc.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(loc.get());
    }

    public record LocationRequest(
            @NotNull UUID orderId,
            @Min(-90) @Max(90) double lat,
            @Min(-180) @Max(180) double lng,
            @NotNull OffsetDateTime timestamp,
            OffsetDateTime lastSeenTimestamp
    ) {}
}


