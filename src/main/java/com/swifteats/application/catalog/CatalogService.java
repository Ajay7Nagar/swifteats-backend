package com.swifteats.application.catalog;

import com.swifteats.domain.catalog.CatalogRepository;
import com.swifteats.domain.catalog.MenuItem;
import com.swifteats.domain.catalog.Restaurant;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CatalogService {
    private final CatalogRepository repository;

    public CatalogService(CatalogRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = "restaurants:list", key = "#city + '|' + #state + '|' + #page + '|' + #pageSize")
    public List<Restaurant> listRestaurants(String city, String state, List<String> tags, int page, int pageSize) {
        return repository.listRestaurants(city, state, tags, page, pageSize);
    }

    public Optional<Restaurant> findRestaurant(UUID id) {
        return repository.findRestaurant(id);
    }

    @Cacheable(cacheNames = "menu", key = "#restaurantId")
    public List<MenuItem> listMenu(UUID restaurantId) {
        return repository.listMenu(restaurantId);
    }
}


