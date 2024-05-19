package com.example.customerarchive.service;

import com.example.customerarchive.model.Customer;

import java.util.List;

public interface CustomerService {
    Customer addCustomer(Customer customer, String username);
    Customer updateCustomer(Long id, Customer customerDetails, String username);
    void deleteCustomer(Long id, String username);
    List<Customer> getAllCustomers(String username);
    Customer getCustomerById(Long id, String username);
    boolean isCustomerOwnedByUser(Long customerId, String username);
}
