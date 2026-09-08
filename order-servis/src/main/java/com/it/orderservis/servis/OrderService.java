package com.it.orderservis.servis;

import com.it.orderservis.dto.OrderDTOInput;
import com.it.orderservis.dto.OrderDTOOutput;
import com.it.orderservis.dto.OrderItemDTOInput;
import com.it.orderservis.dto.OrderItemDTOOutput;
import com.it.orderservis.entity.Order;
import com.it.orderservis.entity.OrderItem;
import com.it.orderservis.repository.OrderItemRepository;
import com.it.orderservis.repository.OrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;


    public OrderDTOOutput createOrder(@Valid OrderDTOInput input) {

        Order order = Order.builder()
                .userId(input.getUserId())
                .totale(BigDecimal.ZERO)
                .stato("CREATED")
                .dataCreazione(LocalDateTime.now())
                .build();

        for (OrderItemDTOInput itemInput : input.getItems()) {

            OrderItem item = OrderItem.builder()
                    .productId(itemInput.getProductId())
                    .quantita(itemInput.getQuantita())
                    .prezzo(BigDecimal.ZERO)
                    .order(order)
                    .build();

            order.getItems().add(item);
        }

        orderRepository.save(order);

        return convertToDTO(order);
    }

    public List<OrderDTOOutput> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

    }

    public OrderDTOOutput getOrderById(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Ordine con id: ("+id +") non trtovato"));
        return convertToDTO(order);
    }

    public void deleteOrder(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ordine non trovato: " + id)
                );

        orderRepository.delete(order);
    }

    private OrderDTOOutput convertToDTO(Order order) {

        List<OrderItemDTOOutput> items = order.getItems()
                .stream()
                .map(item -> OrderItemDTOOutput.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantita(item.getQuantita())
                        .prezzo(item.getPrezzo())
                        .build())
                .collect(Collectors.toList());

        return OrderDTOOutput.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totale(order.getTotale())
                .stato(order.getStato())
                .dataCreazione(order.getDataCreazione())
                .items(items)
                .build();
    }
}
