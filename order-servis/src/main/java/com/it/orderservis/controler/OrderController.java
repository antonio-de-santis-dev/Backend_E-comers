package com.it.orderservis.controler;

import com.it.orderservis.dto.OrderDTOInput;
import com.it.orderservis.dto.OrderDTOOutput;
import com.it.orderservis.servis.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTOOutput> createOrder(
            @Valid @RequestBody OrderDTOInput orderDTOInput
            ){
        OrderDTOOutput order = orderService.createOrder(orderDTOInput);

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }


    @GetMapping
    public ResponseEntity<List<OrderDTOOutput>> getAllOrders() {

        List<OrderDTOOutput> orders = orderService.getAllOrders();

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTOOutput> getOrderById(
            @PathVariable UUID id) {

        OrderDTOOutput order = orderService.getOrderById(id);

        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable UUID id) {

        orderService.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }
}
