package com.swifteats.adapters.persistence.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<RestaurantEntity, UUID> {
    Page<RestaurantEntity> findByCityContainingIgnoreCaseAndStateContainingIgnoreCase(String city, String state, Pageable pageable);
}


