package com.it.orderservis.servis;

import com.it.orderservis.client.NotificationClient;
import com.it.orderservis.client.PaymentClient;
import com.it.orderservis.client.ProductClient;
import com.it.orderservis.client.UserClient;
import com.it.orderservis.dto.*;
import com.it.orderservis.entity.Order;
import com.it.orderservis.entity.OrderItem;
import com.it.orderservis.entity.OrderStatus;
import com.it.orderservis.entity.Shipping;
import com.it.orderservis.exception.InsufficientStockException;
import com.it.orderservis.exception.ResourceNotFoundException;
import com.it.orderservis.exception.ResourceAlreadyExistsException;
import com.it.orderservis.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.it.orderservis.dto.NotificationDTOInput;

import java.math.BigDecimal;
import java.text.Normalizer;
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
       UserDTOOutput user = userClient.findUserById(input.getUserId());
       //1.1. Genero il codice oridine
        String codOrder = generateCodOrder(user);

        // 2. Creo l'ordine
        Order order = Order.builder()
                .codOrder(codOrder)
                .userId(input.getUserId())
                .totale(BigDecimal.ZERO)
                .stato(OrderStatus.CREATED)
                .dataCreazione(LocalDateTime.now())
                .build();
        //2.1. Creo lo Shipping con snapshot dei dati o inseriti al momento o recuperati da user

        String indirizzoDefault = user.getIndirizzoSpedizione();

        if (indirizzoDefault == null || indirizzoDefault.isBlank()) {
            indirizzoDefault = user.getIndirizzoResidenza();
        }

        Shipping shipping;

        if (input.getShipping() != null) {

            ShippingDTOInput shippingInput = input.getShipping();

            shipping = Shipping.builder()
                    .emailContatto(shippingInput.getEmailContatto())
                    .indirizzoSpedizione(shippingInput.getIndirizzoSpedizione())
                    .cap(shippingInput.getCap())
                    .citta(shippingInput.getCitta())
                    .provincia(shippingInput.getProvincia())
                    .regione(shippingInput.getRegione())
                    .paese(shippingInput.getPaese())
                    .order(order)
                    .build();

        } else {

            shipping = Shipping.builder()
                    .emailContatto(user.getEmail())
                    .indirizzoSpedizione(user.getIndirizzoSpedizione())
                    .cap(user.getCap())
                    .citta(user.getCitta())
                    .provincia(user.getProvincia())
                    .regione(user.getRegione())
                    .paese(user.getPaese())
                    .order(order)
                    .build();
        }


        //2.2. Collego Shipping a Order
        order.setShipping(shipping);

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
    public OrderDTOOutput requestCancellation(UUID orderId){

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ordine non trovato con id: " + orderId
                        ));
        if (order.getStato() != OrderStatus.PAID){
            throw new IllegalStateException(
                    "La cancellazione può essere richiesta solo per ordini PAID"
            );
        }

        if (Boolean.TRUE.equals(order.getCancelazioneRichiesta())){
            throw new ResourceAlreadyExistsException(
                    "La cancellazione è già stata richiesta per questo ordine"
            );
        }

        order.setCancelazioneRichiesta(true);
        order.setDataRichiestaCancellazione(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        NotificationDTOInput notificationDTOInput =
                NotificationDTOInput.builder()
                        .userId(updatedOrder.getUserId())
                        .orderId(updatedOrder.getId())
                        .tipo("ORDER_CANCELLATION_REQUESTED")
                        .messaggio(
                                "Richiesta di cancellazione ricevuta per l'ordine "
                                        + updatedOrder.getCodOrder()
                        )
                        .build();

        sendNotificationSafely(notificationDTOInput);
        return convertToDTO(updatedOrder);
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

        List<OrderItemDTOOutput> items = order.getItems()
                .stream()
                .map(item -> OrderItemDTOOutput.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .quantita(item.getQuantita())
                        .prezzo(item.getPrezzo())
                        .build()
                )
                .toList();

        ShippingDTOOutput shippingDTO = null;

        if (order.getShipping() != null) {

            Shipping shipping = order.getShipping();

            shippingDTO = ShippingDTOOutput.builder()
                    .id(shipping.getId())
                    .codOrder(order.getCodOrder())
                    .emailContatto(shipping.getEmailContatto())
                    .indirizzoSpedizione(shipping.getIndirizzoSpedizione())
                    .cap(shipping.getCap())
                    .citta(shipping.getCitta())
                    .provincia(shipping.getProvincia())
                    .regione(shipping.getRegione())
                    .paese(shipping.getPaese())
                    .build();
        }

        return OrderDTOOutput.builder()
                .id(order.getId())
                .codOrder(order.getCodOrder())
                .userId(order.getUserId())
                .totale(order.getTotale())
                .stato(order.getStato())
                .dataCreazione(order.getDataCreazione())
                .items(items)
                .shipping(shippingDTO)
                .build();
    }

    public OrderDTOOutput findByCodOrder(String codOrder){

        Order order = orderRepository.findByCodOrder(codOrder)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ordine non trovato con codice: " + codOrder
                        )
                );
        return convertToDTO(order);
    }

    public List<OrderDTOOutput> findByUserId(UUID userId){

        userClient.findUserById(userId);

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDTO)
                .toList();
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

    private String generateCodOrder(UserDTOOutput user){

        String nomeCod = extractCodePart(user.getNome());
        String cognomeCode = extractCodePart(user.getCognome());
        String cittaCode = extractFirstLetter(user.getCitta());

        Long sequence = orderRepository.getNextOrderSequence();

        return String.format(
                "%s%s-%06d-%s",
                nomeCod,
                cognomeCode,
                sequence,
                cittaCode


        );
    }

    private String extractCodePart(String value) {

        String normalized = normalizeValue(value);

        if (normalized.length() == 1) {
            return normalized + normalized;
        }

        return ""
                + normalized.charAt(0)
                + normalized.charAt(normalized.length() - 1);
    }

    private String extractFirstLetter(String value) {

        String normalized = normalizeValue(value);

        return String.valueOf(normalized.charAt(0));
    }

    private String normalizeValue(String value) {

        String normalized = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
        );

        return normalized
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z]", "")
                .toUpperCase();
    }
}
