package com.it.orderservis.servis;

import com.it.orderservis.client.NotificationClient;
import com.it.orderservis.client.PaymentClient;
import com.it.orderservis.client.ProductClient;
import com.it.orderservis.client.UserClient;
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
import com.it.orderservis.client.NotificationClient;
import com.it.orderservis.dto.NotificationDTOInput;

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
    private final UserClient userClient;
    private final NotificationClient notificationClient;


    @Transactional
    public OrderDTOOutput createOrder(OrderDTOInput input) {

        // 1. Verifica che l'utente esista
        userClient.findUserById(input.getUserId());

        // 2. Creo l'ordine
        Order order = Order.builder()
                .userId(input.getUserId())
                .totale(BigDecimal.ZERO)
                .stato(OrderStatus.CREATED)
                .dataCreazione(LocalDateTime.now())
                .build();

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totale = BigDecimal.ZERO;

        // 3. Verifica prodotti + calcolo totale
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

        // 4. Salva ordine CREATED
        Order savedOrder = orderRepository.save(order);

        // 5. Crea richiesta pagamento
        PaymentDTOInput paymentInput = PaymentDTOInput.builder()
                .orderId(savedOrder.getId())
                .importo(savedOrder.getTotale())
                .metodoPagamento(input.getMetodoPagamento())
                .build();

        // 6. Chiama Payment Service
        PaymentDTOOutput payment =
                paymentClient.createPayment(paymentInput);

        // 7. Gestisce esito pagamento
        if ("SUCCESS".equalsIgnoreCase(payment.getStato())) {

            List<OrderItem> stockDecremented = new ArrayList<>();

            try {
                for (OrderItem item : savedOrder.getItems()) {
                    productClient.decreaseStock(
                            item.getProductId(),
                            item.getQuantita()
                    );

                    stockDecremented.add(item);
                }

                savedOrder.setStato(OrderStatus.PAID);
                NotificationDTOInput notificationInput =
                        NotificationDTOInput.builder()
                                .userId(savedOrder.getUserId())
                                .orderId(savedOrder.getId())
                                .tipo("ORDER_PAID")
                                .messaggio("Ordine pagato con successo")
                                .build();

                notificationClient.createNotification(notificationInput);

            }catch (Exception e){

                for (OrderItem item : stockDecremented) {
                    productClient.increaseStock(
                            item.getProductId(),
                            item.getQuantita()
                    );
                }
                savedOrder.setStato(OrderStatus.FAILED);
                savedOrder.setStato(OrderStatus.FAILED);

                NotificationDTOInput notificationInput =
                        NotificationDTOInput.builder()
                                .userId(savedOrder.getUserId())
                                .orderId(savedOrder.getId())
                                .tipo("ORDER_FAILED")
                                .messaggio("Ordine non completato")
                                .build();

                notificationClient.createNotification(notificationInput);

                orderRepository.save(savedOrder);

                return convertToDTO(savedOrder);
            }
        } else {
            savedOrder.setStato(OrderStatus.FAILED);

            NotificationDTOInput notificationInput =
                    NotificationDTOInput.builder()
                            .userId(savedOrder.getUserId())
                            .orderId(savedOrder.getId())
                            .tipo("ORDER_FAILED")
                            .messaggio("Ordine non completato")
                            .build();

            notificationClient.createNotification(notificationInput);        }

        // 8. Salva stato finale
        Order updatedOrder =
                orderRepository.save(savedOrder);

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
