package com.it.orderservis.servis;

import com.it.orderservis.client.ProductClient;
import com.it.orderservis.dto.*;
import com.it.orderservis.entity.Order;
import com.it.orderservis.entity.OrderItem;
import com.it.orderservis.entity.OrderStatus;
import com.it.orderservis.exception.InsufficientStockException;
import com.it.orderservis.exception.ResourceNotFoundException;
import com.it.orderservis.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional
    public OrderDTOOutput createOrder(OrderDTOInput input) {

        Order order = Order.builder()
                .userId(input.getUserId())
                .totale(BigDecimal.ZERO)
                .stato(OrderStatus.CREATED)
                .dataCreazione(LocalDateTime.now())
                .build();

        List<OrderItem> items = new ArrayList<>();

        BigDecimal totale = BigDecimal.ZERO;

        for (OrderItemDTOInput itemDTOInput : input.getItems()) {

            ProductDTOOutput product = productClient.findProductById(
                    itemDTOInput.getProductId()
            );

            if (!Boolean.TRUE.equals(product.getDisponibile())){
                throw new InsufficientStockException(
                        "Prodotto non disponibile: "
                                + product.getId()
                );
            }

            if (itemDTOInput.getQuantita() > product.getQuantita()){
                throw new InsufficientStockException(
                        "Quantità richiesta non disponibile per il prodotto: "
                                + product.getId()
                );
            }

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantita(itemDTOInput.getQuantita())
                    .prezzo(product.getPrezzo())
                    .order(order)
                    .build();

            items.add(orderItem);

            BigDecimal subtotale =
                    product.getPrezzo()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemDTOInput.getQuantita()
                                    )
                            );
            totale = totale.add(subtotale);
        }
        order.setItems(items);
        order.setTotale(totale);
        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }
    @Transactional(readOnly = true)
    public List<OrderDTOOutput> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

    }
    @Transactional(readOnly = true)
    public OrderDTOOutput getOrderById(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Ordine con id: ("+id +") non trtovato"));
        return convertToDTO(order);
    }
    @Transactional
    public void deleteOrder(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ordine non trovato: " + id)
                );

        orderRepository.delete(order);
    }

    private OrderDTOOutput convertToDTO(Order order) {

        List<OrderItemDTOOutput> items =
                order.getItems()
                        .stream()
                        .map(this::convertItemToDTO)
                        .toList();

        return OrderDTOOutput.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totale(order.getTotale())
                .stato(order.getStato())
                .dataCreazione(order.getDataCreazione())
                .items(items)
                .build();
    }

    private OrderItemDTOOutput convertItemToDTO(
            OrderItem item) {

        return OrderItemDTOOutput.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantita(item.getQuantita())
                .prezzo(item.getPrezzo())
                .build();
    }
}
