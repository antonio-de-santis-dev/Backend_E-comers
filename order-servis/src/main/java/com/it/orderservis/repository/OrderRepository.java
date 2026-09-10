package com.it.orderservis.repository;

import com.it.orderservis.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OrderRepository extends JpaRepository <Order, UUID> {

    @Query(
            value = "SELECT nextval('order_code_seq')",
            nativeQuery = true
    )
    Long getNextOrderSequence();

}
