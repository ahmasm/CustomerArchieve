package com.example.customerarchive.repository;

import com.example.customerarchive.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByUserUsername(String username);
    Optional<Customer> findByCustomerIdAndUserUsername(Long id, String username);

}