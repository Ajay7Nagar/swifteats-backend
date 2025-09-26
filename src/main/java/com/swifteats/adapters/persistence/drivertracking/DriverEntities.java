package com.swifteats.adapters.persistence.drivertracking;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "drivers")
class DriverEntity {
    @Id UUID id;
    String name;
    String phone;
    @Column(name = "created_at") OffsetDateTime createdAt;
}

@Entity
@Table(name = "driver_status")
class DriverStatusEntity {
    @Id @Column(name = "driver_id") UUID driverId;
    @Column(name = "is_online") boolean online;
    @Column(name = "updated_at") OffsetDateTime updatedAt;
}

@Entity
@Table(name = "driver_locations_latest")
class DriverLocationLatestEntity {
    @Id @Column(name = "driver_id") UUID driverId;
    @Column(name = "order_id") UUID orderId;
    double lat;
    double lng;
    @Column(name = "timestamp") OffsetDateTime timestamp;
}

@Entity
@Table(name = "order_driver_assignment")
class OrderDriverAssignmentEntity {
    @Id @Column(name = "order_id") UUID orderId;
    @Column(name = "driver_id") UUID driverId;
    @Column(name = "assigned_at") OffsetDateTime assignedAt;
}


