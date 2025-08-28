package com.swifteats.simulator;

import com.swifteats.dto.DriverLocationUpdateDto;
import com.swifteats.model.Driver;
import com.swifteats.repository.DriverRepository;
import com.swifteats.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Data simulator for testing driver location updates
 * Simulates 50 drivers generating 10 events per second (requirement for local testing)
 */
@Component
public class DriverLocationSimulator implements CommandLineRunner {
    
    private final DriverService driverService;
    private final DriverRepository driverRepository;
    
    @Value("${swifteats.simulator.driver-count:50}")
    private int driverCount;
    
    @Value("${swifteats.simulator.events-per-second:10}")
    private int eventsPerSecond;
    
    @Value("${swifteats.simulator.enabled:true}")
    private boolean simulatorEnabled;
    
    private final Random random = new Random();
    private final List<SimulatedDriver> simulatedDrivers = new ArrayList<>();
    
    // Maharashtra boundaries (approximate)
    private static final double MAHARASHTRA_MIN_LAT = 15.6;
    private static final double MAHARASHTRA_MAX_LAT = 22.0;
    private static final double MAHARASHTRA_MIN_LNG = 72.6;
    private static final double MAHARASHTRA_MAX_LNG = 80.9;
    
    // Major cities in Maharashtra with coordinates
    private static final double[][] MAJOR_CITIES = {
        {19.0760, 72.8777}, // Mumbai
        {18.5204, 73.8567}, // Pune
        {19.7515, 75.7139}, // Aurangabad
        {21.1458, 79.0882}, // Nagpur
        {16.7050, 74.2433}, // Kolhapur
        {19.2183, 72.9781}, // Thane
        {18.6298, 73.7997}, // Lonavala
        {19.9975, 73.7898}  // Nashik
    };
    
    @Autowired
    public DriverLocationSimulator(DriverService driverService, DriverRepository driverRepository) {
        this.driverService = driverService;
        this.driverRepository = driverRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        if (!simulatorEnabled) {
            System.out.println("Driver location simulator is disabled");
            return;
        }
        
        System.out.println("Starting Driver Location Simulator...");
        System.out.println("Creating " + driverCount + " simulated drivers");
        System.out.println("Target events per second: " + eventsPerSecond);
        
        // Create simulated drivers
        createSimulatedDrivers();
        
        // Start location update simulation
        startLocationSimulation();
    }
    
    private void createSimulatedDrivers() {
        for (int i = 1; i <= driverCount; i++) {
            // Check if driver already exists
            String email = "simulator.driver" + i + "@swifteats.com";
            if (driverRepository.existsByEmail(email)) {
                Driver existingDriver = driverRepository.findByEmail(email).orElse(null);
                if (existingDriver != null) {
                    simulatedDrivers.add(new SimulatedDriver(existingDriver));
                }
                continue;
            }
            
            // Create new simulated driver
            Driver driver = new Driver();
            driver.setName("Simulator Driver " + i);
            driver.setEmail(email);
            driver.setPhone("91999000" + String.format("%04d", i));
            driver.setLicenseNumber("MH" + String.format("%08d", i));
            driver.setVehicleType(getRandomVehicleType());
            driver.setVehicleNumber("MH12" + getRandomString(2) + String.format("%04d", i));
            driver.setStatus(Driver.DriverStatus.ONLINE);
            driver.setRating(4.0 + random.nextDouble()); // 4.0 to 5.0 rating
            driver.setTotalDeliveries(random.nextInt(500));
            
            // Set initial position near a major city
            double[] cityCoords = MAJOR_CITIES[random.nextInt(MAJOR_CITIES.length)];
            driver.setCurrentLatitude(cityCoords[0] + (random.nextGaussian() * 0.01)); // Small random offset
            driver.setCurrentLongitude(cityCoords[1] + (random.nextGaussian() * 0.01));
            driver.setLastLocationUpdate(LocalDateTime.now());
            
            driver = driverRepository.save(driver);
            simulatedDrivers.add(new SimulatedDriver(driver));
        }
        
        System.out.println("Created " + simulatedDrivers.size() + " simulated drivers");
    }
    
    private void startLocationSimulation() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
        
        // Calculate delay between events to achieve target events per second
        long delayMs = 1000L / eventsPerSecond;
        
        // Schedule location updates for each driver
        for (SimulatedDriver simDriver : simulatedDrivers) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    generateLocationUpdate(simDriver);
                } catch (Exception e) {
                    System.err.println("Error generating location update for driver " + 
                                     simDriver.getDriver().getId() + ": " + e.getMessage());
                }
            }, random.nextInt(1000), delayMs * driverCount, TimeUnit.MILLISECONDS);
        }
        
        System.out.println("Driver location simulation started with " + eventsPerSecond + " events/second");
        
        // Schedule periodic status updates
        scheduler.scheduleAtFixedRate(this::printSimulationStats, 30, 30, TimeUnit.SECONDS);
    }
    
    private void generateLocationUpdate(SimulatedDriver simDriver) {
        Driver driver = simDriver.getDriver();
        
        // Simulate realistic movement
        double currentLat = driver.getCurrentLatitude();
        double currentLng = driver.getCurrentLongitude();
        
        // Generate new position based on movement pattern
        double[] newPosition = simDriver.generateNextPosition(currentLat, currentLng);
        
        // Create location update
        DriverLocationUpdateDto locationUpdate = new DriverLocationUpdateDto();
        locationUpdate.setDriverId(driver.getId());
        locationUpdate.setLatitude(newPosition[0]);
        locationUpdate.setLongitude(newPosition[1]);
        locationUpdate.setAccuracy(random.nextDouble() * 10 + 5); // 5-15 meters accuracy
        locationUpdate.setSpeed(simDriver.getCurrentSpeed());
        locationUpdate.setHeading(simDriver.getCurrentHeading());
        locationUpdate.setTimestamp(LocalDateTime.now());
        
        // Send location update
        driverService.updateDriverLocation(locationUpdate);
        
        // Update driver's current position
        driver.setCurrentLatitude(newPosition[0]);
        driver.setCurrentLongitude(newPosition[1]);
    }
    
    private void printSimulationStats() {
        long onlineDrivers = simulatedDrivers.stream()
                .mapToLong(sd -> sd.getDriver().getStatus() == Driver.DriverStatus.ONLINE ? 1 : 0)
                .sum();
        
        System.out.println("Simulation Stats - Online Drivers: " + onlineDrivers + "/" + driverCount + 
                          ", Target Events/sec: " + eventsPerSecond);
    }
    
    private String getRandomVehicleType() {
        String[] types = {"Motorcycle", "Scooter", "Bicycle", "Car"};
        return types[random.nextInt(types.length)];
    }
    
    private String getRandomString(int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
    
    /**
     * Inner class to simulate driver movement patterns
     */
    private class SimulatedDriver {
        private final Driver driver;
        private final MovementPattern movementPattern;
        private double currentSpeed; // km/h
        private double currentHeading; // degrees
        private final Random random = new Random();
        
        public SimulatedDriver(Driver driver) {
            this.driver = driver;
            this.movementPattern = MovementPattern.values()[random.nextInt(MovementPattern.values().length)];
            this.currentSpeed = 20 + random.nextDouble() * 40; // 20-60 km/h
            this.currentHeading = random.nextDouble() * 360; // random initial heading
        }
        
        public Driver getDriver() {
            return driver;
        }
        
        public double getCurrentSpeed() {
            return currentSpeed;
        }
        
        public double getCurrentHeading() {
            return currentHeading;
        }
        
        public double[] generateNextPosition(double currentLat, double currentLng) {
            // Simulate different movement patterns
            switch (movementPattern) {
                case RANDOM_WALK:
                    return generateRandomWalk(currentLat, currentLng);
                case CIRCULAR:
                    return generateCircularMovement(currentLat, currentLng);
                case LINEAR:
                    return generateLinearMovement(currentLat, currentLng);
                case STATIONARY:
                    return generateStationaryMovement(currentLat, currentLng);
                default:
                    return new double[]{currentLat, currentLng};
            }
        }
        
        private double[] generateRandomWalk(double lat, double lng) {
            // Random movement within small radius
            double maxDistance = 0.002; // ~200 meters
            double deltaLat = (random.nextGaussian() * maxDistance);
            double deltaLng = (random.nextGaussian() * maxDistance);
            
            // Update speed and heading
            currentSpeed = Math.max(5, currentSpeed + (random.nextGaussian() * 5));
            currentHeading = (currentHeading + (random.nextGaussian() * 30)) % 360;
            
            return new double[]{
                Math.max(MAHARASHTRA_MIN_LAT, Math.min(MAHARASHTRA_MAX_LAT, lat + deltaLat)),
                Math.max(MAHARASHTRA_MIN_LNG, Math.min(MAHARASHTRA_MAX_LNG, lng + deltaLng))
            };
        }
        
        private double[] generateCircularMovement(double lat, double lng) {
            // Circular movement around initial position
            double radius = 0.005; // ~500 meters
            double angle = (System.currentTimeMillis() / 10000.0) % (2 * Math.PI);
            
            currentSpeed = 25 + random.nextGaussian() * 5;
            currentHeading = Math.toDegrees(angle + Math.PI/2) % 360;
            
            return new double[]{
                lat + Math.sin(angle) * radius,
                lng + Math.cos(angle) * radius
            };
        }
        
        private double[] generateLinearMovement(double lat, double lng) {
            // Linear movement in current heading
            double distance = 0.0005; // ~50 meters per update
            double headingRad = Math.toRadians(currentHeading);
            
            // Occasionally change direction
            if (random.nextDouble() < 0.1) {
                currentHeading = (currentHeading + (random.nextGaussian() * 45)) % 360;
            }
            
            currentSpeed = 30 + random.nextGaussian() * 10;
            
            double newLat = lat + Math.sin(headingRad) * distance;
            double newLng = lng + Math.cos(headingRad) * distance;
            
            // Ensure within Maharashtra bounds
            if (newLat < MAHARASHTRA_MIN_LAT || newLat > MAHARASHTRA_MAX_LAT ||
                newLng < MAHARASHTRA_MIN_LNG || newLng > MAHARASHTRA_MAX_LNG) {
                currentHeading = (currentHeading + 180) % 360; // Reverse direction
                return new double[]{lat, lng};
            }
            
            return new double[]{newLat, newLng};
        }
        
        private double[] generateStationaryMovement(double lat, double lng) {
            // Minimal movement (parked/waiting)
            double maxJitter = 0.0001; // ~10 meters
            currentSpeed = 0;
            
            return new double[]{
                lat + (random.nextGaussian() * maxJitter),
                lng + (random.nextGaussian() * maxJitter)
            };
        }
    }
    
    private enum MovementPattern {
        RANDOM_WALK, CIRCULAR, LINEAR, STATIONARY
    }
}
