package com.swifteats.service;

import com.swifteats.dto.MenuItemResponseDto;
import com.swifteats.dto.RestaurantResponseDto;
import com.swifteats.exception.RestaurantNotFoundException;
import com.swifteats.exception.RestaurantNotActiveException;
import com.swifteats.model.MenuItem;
import com.swifteats.model.Restaurant;
import com.swifteats.repository.MenuItemRepository;
import com.swifteats.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Restaurant and Menu operations
 * Optimized for <200ms P99 response time with caching
 */
@Service
@Transactional(readOnly = true)
public class RestaurantService {
    
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    
    @Autowired
    public RestaurantService(RestaurantRepository restaurantRepository, 
                           MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }
    
    /**
     * Get all active restaurants - cached for performance
     */
    @Cacheable(value = "restaurants", key = "'active'")
    public List<RestaurantResponseDto> getActiveRestaurants() {
        List<Restaurant> restaurants = restaurantRepository.findByStatus(Restaurant.RestaurantStatus.ACTIVE);
        return restaurants.stream()
                .map(RestaurantResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get restaurants within radius - cached with location key
     */
    @Cacheable(value = "restaurants", key = "'location_' + #latitude + '_' + #longitude + '_' + #radiusKm")
    public List<RestaurantResponseDto> getRestaurantsNearby(Double latitude, Double longitude, Double radiusKm) {
        List<Restaurant> restaurants = restaurantRepository.findRestaurantsWithinRadius(latitude, longitude, radiusKm);
        return restaurants.stream()
                .map(RestaurantResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get restaurant by ID - cached for performance
     */
    @Cacheable(value = "restaurants", key = "#restaurantId")
    public RestaurantResponseDto getRestaurantById(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        return new RestaurantResponseDto(restaurant);
    }
    
    /**
     * Get restaurant menu - heavily cached for performance
     */
    @Cacheable(value = "menus", key = "#restaurantId")
    public List<MenuItemResponseDto> getRestaurantMenu(Long restaurantId) {
        // Verify restaurant exists and is active
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));
        
        if (restaurant.getStatus() != Restaurant.RestaurantStatus.ACTIVE) {
            throw new RestaurantNotActiveException(restaurantId);
        }
        
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndAvailable(restaurantId, true);
        return menuItems.stream()
                .map(MenuItemResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get menu items by category - cached for performance
     */
    @Cacheable(value = "menus", key = "#restaurantId + '_category_' + #category")
    public List<MenuItemResponseDto> getMenuByCategory(Long restaurantId, String category) {
        List<MenuItem> menuItems = menuItemRepository.findByRestaurantIdAndCategoryAndAvailable(
                restaurantId, category, true);
        return menuItems.stream()
                .map(MenuItemResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Search restaurants by name or cuisine
     */
    @Cacheable(value = "restaurants", key = "'search_' + #searchTerm")
    public List<RestaurantResponseDto> searchRestaurants(String searchTerm) {
        List<Restaurant> restaurants = restaurantRepository.searchRestaurants(searchTerm);
        return restaurants.stream()
                .map(RestaurantResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Search menu items within a restaurant
     */
    @Cacheable(value = "menus", key = "#restaurantId + '_search_' + #searchTerm")
    public List<MenuItemResponseDto> searchMenuItems(Long restaurantId, String searchTerm) {
        List<MenuItem> menuItems = menuItemRepository.searchMenuItems(restaurantId, searchTerm);
        return menuItems.stream()
                .map(MenuItemResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get menu categories for a restaurant - cached for performance
     */
    @Cacheable(value = "categories", key = "#restaurantId")
    public List<String> getMenuCategories(Long restaurantId) {
        return menuItemRepository.findDistinctCategoriesByRestaurantId(restaurantId);
    }
    
    /**
     * Get top-rated restaurants - cached for performance
     */
    @Cacheable(value = "restaurants", key = "'top_rated_' + #minRating")
    public List<RestaurantResponseDto> getTopRatedRestaurants(Double minRating) {
        List<Restaurant> restaurants = restaurantRepository.findTopRatedRestaurants(minRating);
        return restaurants.stream()
                .map(RestaurantResponseDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get restaurants by cuisine type - cached for performance
     */
    @Cacheable(value = "restaurants", key = "'cuisine_' + #cuisineType")
    public List<RestaurantResponseDto> getRestaurantsByCuisine(String cuisineType) {
        List<Restaurant> restaurants = restaurantRepository.findByCuisineTypeIgnoreCase(cuisineType);
        return restaurants.stream()
                .filter(r -> r.getStatus() == Restaurant.RestaurantStatus.ACTIVE)
                .map(RestaurantResponseDto::new)
                .collect(Collectors.toList());
    }
}
