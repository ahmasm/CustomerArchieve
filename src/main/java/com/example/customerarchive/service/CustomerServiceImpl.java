package com.example.customerarchive.service;

import com.example.customerarchive.exception.ResourceNotFoundException;
import com.example.customerarchive.model.Customer;
import com.example.customerarchive.model.User;
import com.example.customerarchive.repository.CustomerRepository;
import com.example.customerarchive.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Customer addCustomer(Customer customer, String username) {
        logger.info("Adding customer for user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found for username :: " + username));
        customer.setUser(user);
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer added successfully for user: {}", username);
        return savedCustomer;
    }

    @Transactional
    public Customer updateCustomer(Long id, Customer customerDetails, String username) {
        logger.info("Updating customer with id: {} for user: {}", id, username);
        Customer existingCustomer = customerRepository.findByCustomerIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        existingCustomer.setName(customerDetails.getName());
        existingCustomer.setEmail(customerDetails.getEmail());
        Customer updatedCustomer = customerRepository.save(existingCustomer);
        logger.info("Customer with id: {} updated successfully for user: {}", id, username);
        return updatedCustomer;
    }

    @Transactional
    public void deleteCustomer(Long id, String username) {
        logger.info("Deleting customer with id: {} for user: {}", id, username);
        Customer existingCustomer = customerRepository.findByCustomerIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        customerRepository.delete(existingCustomer);
        logger.info("Customer with id: {} deleted successfully for user: {}", id, username);
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers(String username) {
        logger.info("Fetching all customers for user: {}", username);
        List<Customer> customers = customerRepository.findByUserUsername(username);
        logger.info("Fetched {} customers for user: {}", customers.size(), username);
        return customers;
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id, String username) {
        logger.info("Fetching customer with id: {} for user: {}", id, username);
        Customer customer = customerRepository.findByCustomerIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
        logger.info("Fetched customer with id: {} for user: {}", id, username);
        return customer;
    }

    @Transactional
    public void saveCustomer(Customer customer) {
        logger.info("Saving customer with id: {}", customer.getCustomerId());
        customerRepository.save(customer);
        logger.info("Customer with id: {} saved successfully", customer.getCustomerId());
    }

    @Transactional(readOnly = true)
    public boolean isCustomerOwnedByUser(Long customerId, String username) {
        logger.info("Checking if customer with id: {} is owned by user: {}", customerId, username);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for this id :: " + customerId));
        boolean isOwned = customer.getUser().getUsername().equals(username);
        logger.info("Customer with id: {} is owned by user: {}: {}", customerId, username, isOwned);
        return isOwned;
    }
}
