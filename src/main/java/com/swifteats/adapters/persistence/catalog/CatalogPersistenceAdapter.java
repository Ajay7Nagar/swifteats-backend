package com.swifteats.adapters.persistence.catalog;

import com.swifteats.domain.catalog.CatalogRepository;
import com.swifteats.domain.catalog.MenuItem;
import com.swifteats.domain.catalog.Restaurant;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CatalogPersistenceAdapter implements CatalogRepository {

    private final RestaurantRepository restaurantRepo;
    private final MenuItemRepository menuItemRepo;

    public CatalogPersistenceAdapter(RestaurantRepository restaurantRepo,
                                     MenuItemRepository menuItemRepo) {
        this.restaurantRepo = restaurantRepo;
        this.menuItemRepo = menuItemRepo;
    }

    @Override
    public List<Restaurant> listRestaurants(String city, String state, List<String> tags, int page, int pageSize) {
        String c = city == null ? "" : city;
        String s = state == null ? "" : state;
        return restaurantRepo.findByCityContainingIgnoreCaseAndStateContainingIgnoreCase(c, s, PageRequest.of(Math.max(0, page - 1), pageSize))
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Restaurant> findRestaurant(UUID restaurantId) {
        return restaurantRepo.findById(restaurantId).map(this::toDomain);
    }

    @Override
    public List<MenuItem> listMenu(UUID restaurantId) {
        return menuItemRepo.findByRestaurantId(restaurantId).stream()
                .map(e -> new MenuItem(e.getId(), e.getRestaurantId(), e.getName(), e.getDescription(), e.getPrice(), e.isAvailable(), e.getUpdatedAt()))
                .toList();
    }

    private Restaurant toDomain(RestaurantEntity e) {
        List<String> tagList = e.getTags() == null || e.getTags().isBlank() ? List.of() : Arrays.asList(e.getTags().split(","));
        return new Restaurant(e.getId(), e.getName(), e.getAddress(), e.getCity(), e.getState(), tagList, e.isOpen(), e.getUpdatedAt());
    }
}


