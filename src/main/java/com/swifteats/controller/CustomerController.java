package com.swifteats.controller;

import com.swifteats.model.Customer;
import com.swifteats.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Customer management operations
 */
@RestController
@RequestMapping("/customers")
@Tag(name = "Customer Management", description = "APIs for customer profile management")
public class CustomerController {
    
    private final CustomerRepository customerRepository;
    
    @Autowired
    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    /**
     * Get all customers
     */
    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve list of all customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }
    
    /**
     * Get customer by ID
     */
    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID", description = "Retrieve customer details by ID")
    public ResponseEntity<Customer> getCustomerById(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        
        Optional<Customer> customer = customerRepository.findById(customerId);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get customer by email
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieve customer details by email address")
    public ResponseEntity<Customer> getCustomerByEmail(
            @Parameter(description = "Customer email") @PathVariable String email) {
        
        Optional<Customer> customer = customerRepository.findByEmail(email);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Create new customer
     */
    @PostMapping
    @Operation(summary = "Create new customer", description = "Register a new customer in the system")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            // Check if customer with email already exists
            if (customerRepository.existsByEmail(customer.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            
            Customer savedCustomer = customerRepository.save(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update customer
     */
    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer", description = "Update customer profile information")
    public ResponseEntity<Customer> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId,
            @Valid @RequestBody Customer customerUpdate) {
        
        return customerRepository.findById(customerId)
                .map(existingCustomer -> {
                    // Update fields
                    existingCustomer.setName(customerUpdate.getName());
                    existingCustomer.setPhone(customerUpdate.getPhone());
                    existingCustomer.setAddress(customerUpdate.getAddress());
                    existingCustomer.setLatitude(customerUpdate.getLatitude());
                    existingCustomer.setLongitude(customerUpdate.getLongitude());
                    
                    Customer savedCustomer = customerRepository.save(existingCustomer);
                    return ResponseEntity.ok(savedCustomer);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Delete customer
     */
    @DeleteMapping("/{customerId}")
    @Operation(summary = "Delete customer", description = "Remove customer from the system")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Search customers by name
     */
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers by name")
    public ResponseEntity<List<Customer>> searchCustomers(
            @Parameter(description = "Search term") @RequestParam String query) {
        
        List<Customer> customers = customerRepository.findAll().stream()
                .filter(customer -> customer.getName().toLowerCase().contains(query.toLowerCase()))
                .toList();
        
        return ResponseEntity.ok(customers);
    }
}

