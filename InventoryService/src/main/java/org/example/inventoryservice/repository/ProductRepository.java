package org.example.inventoryservice.repository;

import org.example.inventoryservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
