package com.swifteats.simulator;

import com.swifteats.model.Customer;
import com.swifteats.model.MenuItem;
import com.swifteats.model.Restaurant;
import com.swifteats.repository.CustomerRepository;
import com.swifteats.repository.MenuItemRepository;
import com.swifteats.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Data initializer to populate sample restaurants, menu items, and customers
 * Runs before the location simulator to ensure data is available
 */
@Component
@Order(1) // Run before location simulator
public class DataInitializer implements CommandLineRunner {
    
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CustomerRepository customerRepository;
    
    @Autowired
    public DataInitializer(RestaurantRepository restaurantRepository,
                          MenuItemRepository menuItemRepository,
                          CustomerRepository customerRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.customerRepository = customerRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (restaurantRepository.count() > 0) {
            System.out.println("Sample data already exists, skipping initialization");
            return;
        }
        
        System.out.println("Initializing sample data...");
        
        // Create restaurants
        createSampleRestaurants();
        
        // Create customers
        createSampleCustomers();
        
        System.out.println("Sample data initialization completed");
    }
    
    private void createSampleRestaurants() {
        List<RestaurantData> restaurantDataList = Arrays.asList(
            new RestaurantData("Maharaja Restaurant", "Mumbai Central, Mumbai", 19.0728, 72.8826, "Indian", 
                Arrays.asList(
                    new MenuItemData("Butter Chicken", "Creamy tomato curry with tender chicken", 280.0, "Main Course"),
                    new MenuItemData("Paneer Makhani", "Rich paneer curry in butter sauce", 250.0, "Main Course"),
                    new MenuItemData("Biryani", "Aromatic basmati rice with spices", 320.0, "Rice"),
                    new MenuItemData("Naan", "Soft Indian bread", 60.0, "Bread"),
                    new MenuItemData("Gulab Jamun", "Sweet milk dumplings", 80.0, "Dessert")
                )),
            
            new RestaurantData("Pizza Palace", "Bandra West, Mumbai", 19.0596, 72.8295, "Italian",
                Arrays.asList(
                    new MenuItemData("Margherita Pizza", "Classic tomato and mozzarella", 350.0, "Pizza"),
                    new MenuItemData("Pepperoni Pizza", "Spicy pepperoni with cheese", 450.0, "Pizza"),
                    new MenuItemData("Caesar Salad", "Fresh lettuce with caesar dressing", 200.0, "Salad"),
                    new MenuItemData("Garlic Bread", "Crispy bread with garlic butter", 150.0, "Appetizer"),
                    new MenuItemData("Tiramisu", "Classic Italian dessert", 180.0, "Dessert")
                )),
            
            new RestaurantData("Spice Garden", "Koregaon Park, Pune", 18.5414, 73.8868, "Indian",
                Arrays.asList(
                    new MenuItemData("Misal Pav", "Spicy Maharashtrian curry", 120.0, "Main Course"),
                    new MenuItemData("Vada Pav", "Mumbai's favorite street food", 80.0, "Snacks"),
                    new MenuItemData("Pav Bhaji", "Mixed vegetable curry with bread", 150.0, "Main Course"),
                    new MenuItemData("Masala Chai", "Spiced Indian tea", 40.0, "Beverages"),
                    new MenuItemData("Puran Poli", "Sweet lentil flatbread", 100.0, "Dessert")
                )),
            
            new RestaurantData("Dragon House", "FC Road, Pune", 18.5204, 73.8567, "Chinese",
                Arrays.asList(
                    new MenuItemData("Chicken Manchurian", "Indo-Chinese chicken dish", 220.0, "Main Course"),
                    new MenuItemData("Fried Rice", "Wok-fried rice with vegetables", 180.0, "Rice"),
                    new MenuItemData("Hakka Noodles", "Stir-fried noodles", 160.0, "Noodles"),
                    new MenuItemData("Spring Rolls", "Crispy vegetable rolls", 140.0, "Appetizer"),
                    new MenuItemData("Date Pancakes", "Sweet pancakes with dates", 120.0, "Dessert")
                )),
            
            new RestaurantData("South Delights", "Aurangabad Station Road", 19.7515, 75.7139, "South Indian",
                Arrays.asList(
                    new MenuItemData("Masala Dosa", "Crispy crepe with potato filling", 120.0, "Main Course"),
                    new MenuItemData("Idli Sambar", "Steamed rice cakes with lentil curry", 80.0, "Main Course"),
                    new MenuItemData("Uttapam", "Thick pancake with vegetables", 100.0, "Main Course"),
                    new MenuItemData("Filter Coffee", "Strong South Indian coffee", 50.0, "Beverages"),
                    new MenuItemData("Rasmalai", "Sweet milk dessert", 90.0, "Dessert")
                )),
            
            new RestaurantData("Burger Junction", "Sitabuldi, Nagpur", 21.1458, 79.0882, "Fast Food",
                Arrays.asList(
                    new MenuItemData("Classic Burger", "Beef burger with lettuce and tomato", 180.0, "Burgers"),
                    new MenuItemData("Chicken Burger", "Grilled chicken burger", 200.0, "Burgers"),
                    new MenuItemData("French Fries", "Crispy golden fries", 80.0, "Sides"),
                    new MenuItemData("Cold Coffee", "Iced coffee with ice cream", 100.0, "Beverages"),
                    new MenuItemData("Chocolate Shake", "Rich chocolate milkshake", 120.0, "Beverages")
                ))
        );
        
        for (RestaurantData restaurantData : restaurantDataList) {
            // Create restaurant
            Restaurant restaurant = new Restaurant(
                restaurantData.name,
                restaurantData.address,
                restaurantData.latitude,
                restaurantData.longitude
            );
            restaurant.setCuisineType(restaurantData.cuisineType);
            restaurant.setPhone("+91-" + (2000000000L + (long)(Math.random() * 1000000000L)));
            restaurant.setAverageDeliveryTime(25 + (int)(Math.random() * 20)); // 25-45 minutes
            restaurant.setMinimumOrderAmount(100.0 + Math.random() * 100); // 100-200 Rs
            restaurant.setDeliveryFee(30.0 + Math.random() * 20); // 30-50 Rs
            restaurant.setRating(3.5 + Math.random() * 1.5); // 3.5-5.0 rating
            restaurant.setTotalReviews(50 + (int)(Math.random() * 500));
            
            restaurant = restaurantRepository.save(restaurant);
            
            // Create menu items
            for (MenuItemData menuItemData : restaurantData.menuItems) {
                MenuItem menuItem = new MenuItem(menuItemData.name, menuItemData.price, restaurant);
                menuItem.setDescription(menuItemData.description);
                menuItem.setCategory(menuItemData.category);
                menuItem.setPreparationTime(5 + (int)(Math.random() * 20)); // 5-25 minutes
                menuItem.setIsVegetarian(Math.random() < 0.4); // 40% vegetarian
                menuItem.setIsVegan(menuItem.getIsVegetarian() && Math.random() < 0.3); // 30% of veg items are vegan
                menuItem.setCalories(200 + (int)(Math.random() * 600)); // 200-800 calories
                
                menuItemRepository.save(menuItem);
            }
        }
        
        System.out.println("Created " + restaurantDataList.size() + " restaurants with menu items");
    }
    
    private void createSampleCustomers() {
        List<CustomerData> customerDataList = Arrays.asList(
            new CustomerData("Rahul Sharma", "rahul.sharma@email.com", "+91-9876543210"),
            new CustomerData("Priya Patel", "priya.patel@email.com", "+91-9876543211"),
            new CustomerData("Amit Kumar", "amit.kumar@email.com", "+91-9876543212"),
            new CustomerData("Sneha Desai", "sneha.desai@email.com", "+91-9876543213"),
            new CustomerData("Vikram Singh", "vikram.singh@email.com", "+91-9876543214"),
            new CustomerData("Kavya Nair", "kavya.nair@email.com", "+91-9876543215"),
            new CustomerData("Arjun Reddy", "arjun.reddy@email.com", "+91-9876543216"),
            new CustomerData("Ananya Iyer", "ananya.iyer@email.com", "+91-9876543217"),
            new CustomerData("Rohan Gupta", "rohan.gupta@email.com", "+91-9876543218"),
            new CustomerData("Meera Joshi", "meera.joshi@email.com", "+91-9876543219")
        );
        
        for (CustomerData customerData : customerDataList) {
            Customer customer = new Customer(customerData.name, customerData.email);
            customer.setPhone(customerData.phone);
            customer.setAddress("Sample Address, Maharashtra");
            customer.setLatitude(19.0 + Math.random() * 3.0); // Random location in Maharashtra
            customer.setLongitude(72.8 + Math.random() * 8.0);
            
            customerRepository.save(customer);
        }
        
        System.out.println("Created " + customerDataList.size() + " sample customers");
    }
    
    // Helper classes for data structure
    private static class RestaurantData {
        String name;
        String address;
        Double latitude;
        Double longitude;
        String cuisineType;
        List<MenuItemData> menuItems;
        
        public RestaurantData(String name, String address, Double latitude, Double longitude, 
                            String cuisineType, List<MenuItemData> menuItems) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.cuisineType = cuisineType;
            this.menuItems = menuItems;
        }
    }
    
    private static class MenuItemData {
        String name;
        String description;
        Double price;
        String category;
        
        public MenuItemData(String name, String description, Double price, String category) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.category = category;
        }
    }
    
    private static class CustomerData {
        String name;
        String email;
        String phone;
        
        public CustomerData(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
    }
}
