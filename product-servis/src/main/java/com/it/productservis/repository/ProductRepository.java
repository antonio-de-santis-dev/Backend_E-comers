package com.it.productservis.repository;

import com.it.productservis.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product,UUID> {
}
