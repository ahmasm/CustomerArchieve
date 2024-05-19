package com.example.customerarchive.controller;

import com.example.customerarchive.model.Customer;
import com.example.customerarchive.security.JwtUtil;
import com.example.customerarchive.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;

    public CustomerController(CustomerService customerService, JwtUtil jwtUtil) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/add")
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer customer, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("Adding customer for user: {}", username);
        Customer createdCustomer = customerService.addCustomer(customer, username);
        logger.info("Customer added successfully for user: {}", username);
        return ResponseEntity.ok(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customerDetails, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("Updating customer with id: {} for user: {}", id, username);
        Customer updatedCustomer = customerService.updateCustomer(id, customerDetails, username);
        logger.info("Customer with id: {} updated successfully for user: {}", id, username);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("Deleting customer with id: {} for user: {}", id, username);
        customerService.deleteCustomer(id, username);
        logger.info("Customer with id: {} deleted successfully for user: {}", id, username);
        return ResponseEntity.ok("Customer deleted successfully");
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Customer>> getAllCustomers(HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("Fetching all customers for user: {}", username);
        List<Customer> customers = customerService.getAllCustomers(username);
        logger.info("Fetched {} customers for user: {}", customers.size(), username);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id, HttpServletRequest request) {
        String username = jwtUtil.extractUsername(jwtUtil.resolveToken(request));
        logger.info("Fetching customer with id: {} for user: {}", id, username);
        Customer customer = customerService.getCustomerById(id, username);
        logger.info("Fetched customer with id: {} for user: {}", id, username);
        return ResponseEntity.ok(customer);
    }
}