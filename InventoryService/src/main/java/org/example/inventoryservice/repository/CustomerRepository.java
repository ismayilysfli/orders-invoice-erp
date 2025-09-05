package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByEmail(String email);
}
