package com.swifteats.dto;

import com.swifteats.model.MenuItem;

/**
 * DTO for MenuItem responses - optimized for fast menu browsing
 */
public class MenuItemResponseDto {
    
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private Boolean available;
    private Integer preparationTime;
    private String spiceLevel;
    private Boolean isVegetarian;
    private Boolean isVegan;
    private Integer calories;
    
    // Constructors
    public MenuItemResponseDto() {}
    
    public MenuItemResponseDto(MenuItem menuItem) {
        this.id = menuItem.getId();
        this.name = menuItem.getName();
        this.description = menuItem.getDescription();
        this.price = menuItem.getPrice();
        this.category = menuItem.getCategory();
        this.imageUrl = menuItem.getImageUrl();
        this.available = menuItem.getAvailable();
        this.preparationTime = menuItem.getPreparationTime();
        this.spiceLevel = menuItem.getSpiceLevel();
        this.isVegetarian = menuItem.getIsVegetarian();
        this.isVegan = menuItem.getIsVegan();
        this.calories = menuItem.getCalories();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    
    public Integer getPreparationTime() { return preparationTime; }
    public void setPreparationTime(Integer preparationTime) { this.preparationTime = preparationTime; }
    
    public String getSpiceLevel() { return spiceLevel; }
    public void setSpiceLevel(String spiceLevel) { this.spiceLevel = spiceLevel; }
    
    public Boolean getIsVegetarian() { return isVegetarian; }
    public void setIsVegetarian(Boolean isVegetarian) { this.isVegetarian = isVegetarian; }
    
    public Boolean getIsVegan() { return isVegan; }
    public void setIsVegan(Boolean isVegan) { this.isVegan = isVegan; }
    
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
}
