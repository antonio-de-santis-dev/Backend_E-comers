package com.it.orderservis.repository;

import com.it.orderservis.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository <Order, UUID> {

    Optional<Order> findByCodOrder(String codOrder);

    List<Order> findByUserId(UUID id);

    @Query(
            value = "SELECT nextval('order_code_seq')",
            nativeQuery = true
    )
    Long getNextOrderSequence();

}
