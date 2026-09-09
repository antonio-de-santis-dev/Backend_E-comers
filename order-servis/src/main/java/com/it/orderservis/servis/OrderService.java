package com.it.orderservis.servis;

import com.it.orderservis.client.PaymentClient;
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
    private final PaymentClient paymentClient;


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

        for (OrderItemDTOInput itemInput : input.getItems()) {

            ProductDTOOutput product =
                    productClient.findProductById(itemInput.getProductId());

            if (!Boolean.TRUE.equals(product.getDisponibile())) {
                throw new InsufficientStockException(
                        "Prodotto non disponibile: " + product.getId()
                );
            }

            if (itemInput.getQuantita() > product.getQuantita()) {
                throw new InsufficientStockException(
                        "Quantità richiesta non disponibile per il prodotto: "
                                + product.getId()
                );
            }

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .quantita(itemInput.getQuantita())
                    .prezzo(product.getPrezzo())
                    .order(order)
                    .build();

            items.add(orderItem);

            BigDecimal subtotale =
                    product.getPrezzo()
                            .multiply(
                                    BigDecimal.valueOf(itemInput.getQuantita())
                            );

            totale = totale.add(subtotale);
        }

        order.setItems(items);
        order.setTotale(totale);

        // 1. SALVO L'ORDINE COME CREATED
        Order savedOrder = orderRepository.save(order);

        // 2. CREO LA RICHIESTA DI PAGAMENTO
        PaymentDTOInput paymentInput = PaymentDTOInput.builder()
                .orderId(savedOrder.getId())
                .importo(savedOrder.getTotale())
                .metodoPagamento(input.getMetodoPagamento())
                .build();

        // 3. CHIAMO PAYMENT SERVICE
        PaymentDTOOutput payment =
                paymentClient.createPayment(paymentInput);

        // 4. CONTROLLO RISULTATO DEL PAGAMENTO
        if ("SUCCESS".equalsIgnoreCase(payment.getStato())) {

            // 5. SE PAGAMENTO OK, DECREMENTO LO STOCK
            for (OrderItem item : savedOrder.getItems()) {

                productClient.decreaseStock(
                        item.getProductId(),
                        item.getQuantita()
                );
            }

            // 6. ORDINE PAGATO
            savedOrder.setStato(OrderStatus.PAID);

        } else {

            // 7. PAGAMENTO FALLITO
            savedOrder.setStato(OrderStatus.FAILED);
        }

        // 8. SALVO IL NUOVO STATO DELL'ORDINE
        Order updatedOrder =
                orderRepository.save(savedOrder);

        // 9. RITORNO IL DTO
        return convertToDTO(updatedOrder);
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
