package com.swifteats.adapters.persistence.drivertracking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface DriverRepo extends JpaRepository<DriverEntity, UUID> {}
interface DriverStatusRepo extends JpaRepository<DriverStatusEntity, UUID> {}
interface DriverLocationLatestRepo extends JpaRepository<DriverLocationLatestEntity, UUID> {
    Optional<DriverLocationLatestEntity> findByOrderId(UUID orderId);
}
interface OrderDriverAssignmentRepo extends JpaRepository<OrderDriverAssignmentEntity, UUID> {}


