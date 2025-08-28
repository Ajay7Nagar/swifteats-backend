package com.swifteats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swifteats.dto.MenuItemResponseDto;
import com.swifteats.dto.RestaurantResponseDto;
import com.swifteats.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RestaurantController
 * Tests REST API endpoints and JSON serialization
 */
@WebMvcTest(RestaurantController.class)
class RestaurantControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private RestaurantService restaurantService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private RestaurantResponseDto testRestaurant;
    private MenuItemResponseDto testMenuItem;
    
    @BeforeEach
    void setUp() {
        testRestaurant = new RestaurantResponseDto();
        testRestaurant.setId(1L);
        testRestaurant.setName("Test Restaurant");
        testRestaurant.setAddress("123 Test Street, Mumbai");
        testRestaurant.setLatitude(19.0760);
        testRestaurant.setLongitude(72.8777);
        testRestaurant.setStatus("ACTIVE");
        testRestaurant.setCuisineType("Indian");
        testRestaurant.setRating(4.5);
        testRestaurant.setAverageDeliveryTime(30);
        testRestaurant.setDeliveryFee(50.0);
        testRestaurant.setMinimumOrderAmount(150.0);
        
        testMenuItem = new MenuItemResponseDto();
        testMenuItem.setId(1L);
        testMenuItem.setName("Test Item");
        testMenuItem.setDescription("Test Description");
        testMenuItem.setPrice(299.0);
        testMenuItem.setCategory("Main Course");
        testMenuItem.setAvailable(true);
        testMenuItem.setIsVegetarian(true);
        testMenuItem.setCalories(450);
    }
    
    @Test
    void getAllActiveRestaurants_ShouldReturnActiveRestaurants() throws Exception {
        // Given
        List<RestaurantResponseDto> restaurants = Arrays.asList(testRestaurant);
        when(restaurantService.getActiveRestaurants()).thenReturn(restaurants);
        
        // When & Then
        mockMvc.perform(get("/restaurants")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Restaurant")))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$[0].cuisineType", is("Indian")))
                .andExpect(jsonPath("$[0].rating", is(4.5)));
    }
    
    @Test
    void getNearbyRestaurants_ShouldReturnRestaurantsWithinRadius() throws Exception {
        // Given
        List<RestaurantResponseDto> restaurants = Arrays.asList(testRestaurant);
        when(restaurantService.getRestaurantsNearby(anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(restaurants);
        
        // When & Then
        mockMvc.perform(get("/restaurants/nearby")
                .param("latitude", "19.0760")
                .param("longitude", "72.8777")
                .param("radius", "5.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].latitude", is(19.0760)))
                .andExpect(jsonPath("$[0].longitude", is(72.8777)));
    }
    
    @Test
    void getNearbyRestaurants_WithDefaultRadius_ShouldUseDefaultValue() throws Exception {
        // Given
        List<RestaurantResponseDto> restaurants = Arrays.asList(testRestaurant);
        when(restaurantService.getRestaurantsNearby(19.0760, 72.8777, 5.0))
                .thenReturn(restaurants);
        
        // When & Then
        mockMvc.perform(get("/restaurants/nearby")
                .param("latitude", "19.0760")
                .param("longitude", "72.8777")
                // No radius parameter - should use default 5.0
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    void getRestaurantById_WhenRestaurantExists_ShouldReturnRestaurant() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(1L)).thenReturn(testRestaurant);
        
        // When & Then
        mockMvc.perform(get("/restaurants/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Restaurant")))
                .andExpect(jsonPath("$.address", is("123 Test Street, Mumbai")))
                .andExpect(jsonPath("$.deliveryFee", is(50.0)))
                .andExpect(jsonPath("$.minimumOrderAmount", is(150.0)));
    }
    
    @Test
    void getRestaurantMenu_ShouldReturnMenuItems() throws Exception {
        // Given
        List<MenuItemResponseDto> menuItems = Arrays.asList(testMenuItem);
        when(restaurantService.getRestaurantMenu(1L)).thenReturn(menuItems);
        
        // When & Then
        mockMvc.perform(get("/restaurants/1/menu")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")))
                .andExpect(jsonPath("$[0].price", is(299.0)))
                .andExpect(jsonPath("$[0].category", is("Main Course")))
                .andExpect(jsonPath("$[0].available", is(true)))
                .andExpect(jsonPath("$[0].isVegetarian", is(true)))
                .andExpect(jsonPath("$[0].calories", is(450)));
    }
    
    @Test
    void getMenuByCategory_ShouldReturnMenuItemsInCategory() throws Exception {
        // Given
        List<MenuItemResponseDto> menuItems = Arrays.asList(testMenuItem);
        when(restaurantService.getMenuByCategory(1L, "Main Course")).thenReturn(menuItems);
        
        // When & Then
        mockMvc.perform(get("/restaurants/1/menu/category/Main Course")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Main Course")));
    }
    
    @Test
    void getMenuCategories_ShouldReturnCategoryList() throws Exception {
        // Given
        List<String> categories = Arrays.asList("Main Course", "Appetizer", "Dessert");
        when(restaurantService.getMenuCategories(1L)).thenReturn(categories);
        
        // When & Then
        mockMvc.perform(get("/restaurants/1/menu/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Main Course")))
                .andExpect(jsonPath("$[1]", is("Appetizer")))
                .andExpect(jsonPath("$[2]", is("Dessert")));
    }
    
    @Test
    void searchRestaurants_ShouldReturnMatchingRestaurants() throws Exception {
        // Given
        List<RestaurantResponseDto> restaurants = Arrays.asList(testRestaurant);
        when(restaurantService.searchRestaurants("test")).thenReturn(restaurants);
        
        // When & Then
        mockMvc.perform(get("/restaurants/search")
                .param("query", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Restaurant")));
    }
    
    @Test
    void searchMenuItems_ShouldReturnMatchingMenuItems() throws Exception {
        // Given
        List<MenuItemResponseDto> menuItems = Arrays.asList(testMenuItem);
        when(restaurantService.searchMenuItems(1L, "test")).thenReturn(menuItems);
        
        // When & Then
        mockMvc.perform(get("/restaurants/1/menu/search")
                .param("query", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Test Item")));
    }
    
    @Test
    void getRestaurantsByCuisine_ShouldReturnRestaurantsOfSpecificCuisine() throws Exception {
        // Given
        List<RestaurantResponseDto> restaurants = Arrays.asList(testRestaurant);
        when(restaurantService.getRestaurantsByCuisine("Indian")).thenReturn(restaurants);
        
        // When & Then
        mockMvc.perform(get("/restaurants/cuisine/Indian")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cuisineType", is("Indian")));
    }
    
    @Test
    void getTopRatedRestaurants_ShouldReturnHighRatedRestaurants() throws Exception {
        // Given
        List<RestaurantResponseDto> restaurants = Arrays.asList(testRestaurant);
        when(restaurantService.getTopRatedRestaurants(4.0)).thenReturn(restaurants);
        
        // When & Then
        mockMvc.perform(get("/restaurants/top-rated")
                .param("minRating", "4.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rating", is(4.5)));
    }
    
    @Test
    void getTopRatedRestaurants_WithDefaultRating_ShouldUseDefaultValue() throws Exception {
        // Given
        List<RestaurantResponseDto> restaurants = Arrays.asList(testRestaurant);
        when(restaurantService.getTopRatedRestaurants(4.0)).thenReturn(restaurants);
        
        // When & Then
        mockMvc.perform(get("/restaurants/top-rated")
                // No minRating parameter - should use default 4.0
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    void getRestaurantById_WhenServiceThrowsException_ShouldReturn404() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(999L))
                .thenThrow(new com.swifteats.exception.RestaurantNotFoundException(999L));
        
        // When & Then
        mockMvc.perform(get("/restaurants/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void getAllActiveRestaurants_WhenNoRestaurants_ShouldReturnEmptyList() throws Exception {
        // Given
        when(restaurantService.getActiveRestaurants()).thenReturn(Arrays.asList());
        
        // When & Then
        mockMvc.perform(get("/restaurants")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
