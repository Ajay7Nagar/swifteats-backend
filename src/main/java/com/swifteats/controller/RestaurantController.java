package com.swifteats.controller;

import com.swifteats.dto.MenuItemResponseDto;
import com.swifteats.dto.RestaurantResponseDto;
import com.swifteats.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Restaurant and Menu operations
 * Optimized for <200ms P99 response time with caching
 */
@RestController
@RequestMapping("/restaurants")
@Tag(name = "Restaurant Management", description = "APIs for restaurant and menu operations")
public class RestaurantController {
    
    private final RestaurantService restaurantService;
    
    @Autowired
    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }
    
    /**
     * Get all active restaurants
     */
    @GetMapping
    @Operation(summary = "Get all active restaurants", description = "Retrieve list of all active restaurants")
    public ResponseEntity<List<RestaurantResponseDto>> getAllActiveRestaurants() {
        List<RestaurantResponseDto> restaurants = restaurantService.getActiveRestaurants();
        return ResponseEntity.ok(restaurants);
    }
    
    /**
     * Get restaurants near a location
     */
    @GetMapping("/nearby")
    @Operation(summary = "Get nearby restaurants", description = "Find restaurants within specified radius of coordinates")
    public ResponseEntity<List<RestaurantResponseDto>> getNearbyRestaurants(
            @Parameter(description = "Latitude coordinate") @RequestParam Double latitude,
            @Parameter(description = "Longitude coordinate") @RequestParam Double longitude,
            @Parameter(description = "Search radius in kilometers", example = "5.0") @RequestParam(defaultValue = "5.0") Double radius) {
        
        List<RestaurantResponseDto> restaurants = restaurantService.getRestaurantsNearby(latitude, longitude, radius);
        return ResponseEntity.ok(restaurants);
    }
    
    /**
     * Get restaurant by ID
     */
    @GetMapping("/{restaurantId}")
    @Operation(summary = "Get restaurant by ID", description = "Retrieve restaurant details by ID")
    public ResponseEntity<RestaurantResponseDto> getRestaurantById(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        RestaurantResponseDto restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }
    
    /**
     * Get restaurant menu
     */
    @GetMapping("/{restaurantId}/menu")
    @Operation(summary = "Get restaurant menu", description = "Retrieve complete menu for a restaurant")
    public ResponseEntity<List<MenuItemResponseDto>> getRestaurantMenu(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        List<MenuItemResponseDto> menu = restaurantService.getRestaurantMenu(restaurantId);
        return ResponseEntity.ok(menu);
    }
    
    /**
     * Get menu items by category
     */
    @GetMapping("/{restaurantId}/menu/category/{category}")
    @Operation(summary = "Get menu by category", description = "Retrieve menu items for a specific category")
    public ResponseEntity<List<MenuItemResponseDto>> getMenuByCategory(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Menu category") @PathVariable String category) {
        
        List<MenuItemResponseDto> menuItems = restaurantService.getMenuByCategory(restaurantId, category);
        return ResponseEntity.ok(menuItems);
    }
    
    /**
     * Get menu categories for a restaurant
     */
    @GetMapping("/{restaurantId}/menu/categories")
    @Operation(summary = "Get menu categories", description = "Retrieve all menu categories for a restaurant")
    public ResponseEntity<List<String>> getMenuCategories(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        List<String> categories = restaurantService.getMenuCategories(restaurantId);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Search restaurants
     */
    @GetMapping("/search")
    @Operation(summary = "Search restaurants", description = "Search restaurants by name or cuisine type")
    public ResponseEntity<List<RestaurantResponseDto>> searchRestaurants(
            @Parameter(description = "Search term") @RequestParam String query) {
        
        List<RestaurantResponseDto> restaurants = restaurantService.searchRestaurants(query);
        return ResponseEntity.ok(restaurants);
    }
    
    /**
     * Search menu items within a restaurant
     */
    @GetMapping("/{restaurantId}/menu/search")
    @Operation(summary = "Search menu items", description = "Search menu items within a restaurant")
    public ResponseEntity<List<MenuItemResponseDto>> searchMenuItems(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Search term") @RequestParam String query) {
        
        List<MenuItemResponseDto> menuItems = restaurantService.searchMenuItems(restaurantId, query);
        return ResponseEntity.ok(menuItems);
    }
    
    /**
     * Get restaurants by cuisine type
     */
    @GetMapping("/cuisine/{cuisineType}")
    @Operation(summary = "Get restaurants by cuisine", description = "Retrieve restaurants by cuisine type")
    public ResponseEntity<List<RestaurantResponseDto>> getRestaurantsByCuisine(
            @Parameter(description = "Cuisine type") @PathVariable String cuisineType) {
        
        List<RestaurantResponseDto> restaurants = restaurantService.getRestaurantsByCuisine(cuisineType);
        return ResponseEntity.ok(restaurants);
    }
    
    /**
     * Get top-rated restaurants
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top-rated restaurants", description = "Retrieve restaurants with rating above threshold")
    public ResponseEntity<List<RestaurantResponseDto>> getTopRatedRestaurants(
            @Parameter(description = "Minimum rating threshold", example = "4.0") @RequestParam(defaultValue = "4.0") Double minRating) {
        
        List<RestaurantResponseDto> restaurants = restaurantService.getTopRatedRestaurants(minRating);
        return ResponseEntity.ok(restaurants);
    }
}
