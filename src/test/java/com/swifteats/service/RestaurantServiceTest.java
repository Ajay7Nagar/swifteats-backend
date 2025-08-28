package com.swifteats.service;

import com.swifteats.dto.MenuItemResponseDto;
import com.swifteats.dto.RestaurantResponseDto;
import com.swifteats.model.MenuItem;
import com.swifteats.model.Restaurant;
import com.swifteats.repository.MenuItemRepository;
import com.swifteats.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RestaurantService
 * Tests caching behavior and performance optimizations
 */
@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {
    
    @Mock
    private RestaurantRepository restaurantRepository;
    
    @Mock
    private MenuItemRepository menuItemRepository;
    
    @InjectMocks
    private RestaurantService restaurantService;
    
    private Restaurant testRestaurant;
    private MenuItem testMenuItem;
    
    @BeforeEach
    void setUp() {
        testRestaurant = new Restaurant();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Test Street");
        testRestaurant.setLatitude(19.0760);
        testRestaurant.setLongitude(72.8777);
        testRestaurant.setStatus(Restaurant.RestaurantStatus.ACTIVE);
        testRestaurant.setCuisineType("Indian");
        testRestaurant.setRating(4.5);
        
        testMenuItem = new MenuItem();
        testMenuItem.setId(1L);
        testMenuItem.setName("Test Item");
        testMenuItem.setDescription("Test Description");
        testMenuItem.setPrice(299.0);
        testMenuItem.setCategory("Main Course");
        testMenuItem.setAvailable(true);
        testMenuItem.setRestaurant(testRestaurant);
    }
    
    @Test
    void getActiveRestaurants_ShouldReturnActiveRestaurants() {
        // Given
        List<Restaurant> activeRestaurants = Arrays.asList(testRestaurant);
        when(restaurantRepository.findByStatus(Restaurant.RestaurantStatus.ACTIVE))
                .thenReturn(activeRestaurants);
        
        // When
        List<RestaurantResponseDto> result = restaurantService.getActiveRestaurants();
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Test Restaurant");
        assertThat(result.get(0).getStatus()).isEqualTo("ACTIVE");
        
        verify(restaurantRepository).findByStatus(Restaurant.RestaurantStatus.ACTIVE);
    }
    
    @Test
    void getRestaurantsNearby_ShouldReturnNearbyRestaurants() {
        // Given
        Double latitude = 19.0760;
        Double longitude = 72.8777;
        Double radius = 5.0;
        List<Restaurant> nearbyRestaurants = Arrays.asList(testRestaurant);
        
        when(restaurantRepository.findRestaurantsWithinRadius(latitude, longitude, radius))
                .thenReturn(nearbyRestaurants);
        
        // When
        List<RestaurantResponseDto> result = restaurantService.getRestaurantsNearby(latitude, longitude, radius);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLatitude()).isEqualTo(latitude);
        assertThat(result.get(0).getLongitude()).isEqualTo(longitude);
        
        verify(restaurantRepository).findRestaurantsWithinRadius(latitude, longitude, radius);
    }
    
    @Test
    void getRestaurantById_WhenRestaurantExists_ShouldReturnRestaurant() {
        // Given
        Long restaurantId = 1L;
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(testRestaurant));
        
        // When
        RestaurantResponseDto result = restaurantService.getRestaurantById(restaurantId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(restaurantId);
        assertThat(result.getName()).isEqualTo("Test Restaurant");
        
        verify(restaurantRepository).findById(restaurantId);
    }
    
    @Test
    void getRestaurantById_WhenRestaurantNotExists_ShouldThrowException() {
        // Given
        Long restaurantId = 999L;
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> restaurantService.getRestaurantById(restaurantId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Restaurant not found");
        
        verify(restaurantRepository).findById(restaurantId);
    }
    
    @Test
    void getRestaurantMenu_WhenRestaurantIsActive_ShouldReturnMenu() {
        // Given
        Long restaurantId = 1L;
        List<MenuItem> menuItems = Arrays.asList(testMenuItem);
        
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(testRestaurant));
        when(menuItemRepository.findByRestaurantIdAndAvailable(restaurantId, true))
                .thenReturn(menuItems);
        
        // When
        List<MenuItemResponseDto> result = restaurantService.getRestaurantMenu(restaurantId);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Test Item");
        assertThat(result.get(0).getPrice()).isEqualTo(299.0);
        assertThat(result.get(0).getAvailable()).isTrue();
        
        verify(restaurantRepository).findById(restaurantId);
        verify(menuItemRepository).findByRestaurantIdAndAvailable(restaurantId, true);
    }
    
    @Test
    void getRestaurantMenu_WhenRestaurantNotActive_ShouldThrowException() {
        // Given
        Long restaurantId = 1L;
        testRestaurant.setStatus(Restaurant.RestaurantStatus.CLOSED);
        
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(testRestaurant));
        
        // When/Then
        assertThatThrownBy(() -> restaurantService.getRestaurantMenu(restaurantId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Restaurant is not active");
        
        verify(restaurantRepository).findById(restaurantId);
        verify(menuItemRepository, never()).findByRestaurantIdAndAvailable(anyLong(), any());
    }
    
    @Test
    void getMenuByCategory_ShouldReturnMenuItemsByCategory() {
        // Given
        Long restaurantId = 1L;
        String category = "Main Course";
        List<MenuItem> menuItems = Arrays.asList(testMenuItem);
        
        when(menuItemRepository.findByRestaurantIdAndCategoryAndAvailable(restaurantId, category, true))
                .thenReturn(menuItems);
        
        // When
        List<MenuItemResponseDto> result = restaurantService.getMenuByCategory(restaurantId, category);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(category);
        
        verify(menuItemRepository).findByRestaurantIdAndCategoryAndAvailable(restaurantId, category, true);
    }
    
    @Test
    void searchRestaurants_ShouldReturnMatchingRestaurants() {
        // Given
        String searchTerm = "test";
        List<Restaurant> restaurants = Arrays.asList(testRestaurant);
        
        when(restaurantRepository.searchRestaurants(searchTerm)).thenReturn(restaurants);
        
        // When
        List<RestaurantResponseDto> result = restaurantService.searchRestaurants(searchTerm);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase(searchTerm);
        
        verify(restaurantRepository).searchRestaurants(searchTerm);
    }
    
    @Test
    void searchMenuItems_ShouldReturnMatchingMenuItems() {
        // Given
        Long restaurantId = 1L;
        String searchTerm = "test";
        List<MenuItem> menuItems = Arrays.asList(testMenuItem);
        
        when(menuItemRepository.searchMenuItems(restaurantId, searchTerm)).thenReturn(menuItems);
        
        // When
        List<MenuItemResponseDto> result = restaurantService.searchMenuItems(restaurantId, searchTerm);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).containsIgnoringCase(searchTerm);
        
        verify(menuItemRepository).searchMenuItems(restaurantId, searchTerm);
    }
    
    @Test
    void getMenuCategories_ShouldReturnDistinctCategories() {
        // Given
        Long restaurantId = 1L;
        List<String> categories = Arrays.asList("Main Course", "Appetizer", "Dessert");
        
        when(menuItemRepository.findDistinctCategoriesByRestaurantId(restaurantId))
                .thenReturn(categories);
        
        // When
        List<String> result = restaurantService.getMenuCategories(restaurantId);
        
        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Main Course", "Appetizer", "Dessert");
        
        verify(menuItemRepository).findDistinctCategoriesByRestaurantId(restaurantId);
    }
    
    @Test
    void getTopRatedRestaurants_ShouldReturnHighRatedRestaurants() {
        // Given
        Double minRating = 4.0;
        List<Restaurant> topRatedRestaurants = Arrays.asList(testRestaurant);
        
        when(restaurantRepository.findTopRatedRestaurants(minRating))
                .thenReturn(topRatedRestaurants);
        
        // When
        List<RestaurantResponseDto> result = restaurantService.getTopRatedRestaurants(minRating);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isGreaterThanOrEqualTo(minRating);
        
        verify(restaurantRepository).findTopRatedRestaurants(minRating);
    }
    
    @Test
    void getRestaurantsByCuisine_ShouldReturnRestaurantsByCuisineType() {
        // Given
        String cuisineType = "Indian";
        List<Restaurant> restaurants = Arrays.asList(testRestaurant);
        
        when(restaurantRepository.findByCuisineTypeIgnoreCase(cuisineType))
                .thenReturn(restaurants);
        
        // When
        List<RestaurantResponseDto> result = restaurantService.getRestaurantsByCuisine(cuisineType);
        
        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCuisineType()).isEqualToIgnoringCase(cuisineType);
        
        verify(restaurantRepository).findByCuisineTypeIgnoreCase(cuisineType);
    }
}
