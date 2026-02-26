package com.volunteer.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.volunteer.backend.entity.Product;
import com.volunteer.backend.enums.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStatusNot(ProductStatus status, Pageable pageable);

    Page<Product> findByStatusNotAndNameContainingIgnoreCase(ProductStatus status, String name, Pageable pageable);
}
