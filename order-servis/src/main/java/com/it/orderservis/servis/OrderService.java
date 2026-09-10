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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.it.orderservis.dto.NotificationDTOInput;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
                // 8. Provo a decrementare lo stock di tutti i prodotti
                for (OrderItem item : savedOrder.getItems()) {
                    productClient.decreaseStock(
                            item.getProductId(),
                            item.getQuantita()
                    );

                    stockDecremented.add(item);
                }

                savedOrder.setStato(OrderStatus.PAID);

            }catch (Exception e){

                // 9. Compensazione: ripristino solamente gli stock già decrementati
                for (OrderItem item : stockDecremented) {

                    try {

                        productClient.increaseStock(
                                item.getProductId(),
                                item.getQuantita()
                        );
                    }catch (Exception compensationException){

                        log.error(
                                "Errore durante il ripristino dello stock del prodotto {} per ordine {}: {}",
                                item.getProductId(),
                                savedOrder.getId(),
                                compensationException.getMessage()
                        );
                    }
                }
                savedOrder.setStato(OrderStatus.FAILED);

                log.error(
                        "Errore durante il decremento stock per ordine {}: {}",
                        savedOrder.getId(),
                        e.getMessage()

                );
            }
        } else {
            savedOrder.setStato(OrderStatus.FAILED);
        }

        // 10. Salvo SEMPRE lo stato finale dell'ordine
        Order updatedOrder =
                orderRepository.save(savedOrder);

        // 11. La notifica avviene DOPO il salvataggio dello stato
        if (updatedOrder.getStato() == OrderStatus.PAID){
            NotificationDTOInput notificationInput =
                    NotificationDTOInput.builder()
                            .userId(updatedOrder.getUserId())
                            .orderId(updatedOrder.getId())
                            .tipo("ORDER_PAID")
                            .messaggio("Ordine pagato con successo")
                            .build();

            sendNotificationSafely(notificationInput);
        } else if (updatedOrder.getStato() == OrderStatus.FAILED){
            NotificationDTOInput notificationInput =
                    NotificationDTOInput.builder()
                            .userId(updatedOrder.getUserId())
                            .orderId(updatedOrder.getId())
                            .tipo("ORDER_FAILED")
                            .messaggio("Ordine non completato")
                            .build();

            sendNotificationSafely(notificationInput);
        }

        // 12. Ritorno il risultato finale
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

    private void sendNotificationSafely(NotificationDTOInput notificationInput) {

        try {
            notificationClient.createNotification(notificationInput);

        } catch (Exception ex) {

            log.error(
                    "Errore durante l'invio della notifica per ordine {}: {}",
                    notificationInput.getOrderId(),
                    ex.getMessage()
            );
        }
    }
}
