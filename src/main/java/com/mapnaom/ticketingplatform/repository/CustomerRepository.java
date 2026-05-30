package com.mapnaom.ticketingmanagerserver.repository;

import com.mapnaom.ticketingmanagerserver.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Customer> findByUsername(String username);
}