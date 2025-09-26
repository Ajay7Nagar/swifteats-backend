package com.swifteats.domain.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogRepository {
    List<Restaurant> listRestaurants(String city, String state, List<String> tags, int page, int pageSize);
    Optional<Restaurant> findRestaurant(UUID restaurantId);
    List<MenuItem> listMenu(UUID restaurantId);
}


