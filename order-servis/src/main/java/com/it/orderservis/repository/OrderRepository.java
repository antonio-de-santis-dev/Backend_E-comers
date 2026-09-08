package com.it.orderservis.repository;

import com.it.orderservis.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository <Order, UUID> {
}
