package com.it.paymentservice.servis;

import com.it.paymentservice.dto.PaymentDTOInput;
import com.it.paymentservice.dto.PaymentDTOOutput;
import com.it.paymentservice.entity.Payment;
import com.it.paymentservice.entity.PaymentStatus;
import com.it.paymentservice.exception.ResourceNotFoundException;
import com.it.paymentservice.payment.PaymentProcessor;
import com.it.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServis {
    private final PaymentRepository paymentRepository;
    private final PaymentProcessor paymentProcessor;

    @Transactional
    public PaymentDTOOutput createPayment(PaymentDTOInput input) {

        PaymentStatus paymentStatus = paymentProcessor.processPayment(input.getImporto());


        Payment payment = Payment.builder()
                .orderId(input.getOrderId())
                .importo(input.getImporto())
                .metodoPagamento(input.getMetodoPagamento())
                .stato(paymentStatus)
                .dataCreazione(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return convertToDTO(savedPayment);
    }
    @Transactional(readOnly = true)
    public List<PaymentDTOOutput> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public PaymentDTOOutput getPaymentById(UUID id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pagamento non trovato: " + id)
                );

        return convertToDTO(payment);
    }
    @Transactional
    public PaymentDTOOutput updatePayment( UUID id, PaymentDTOInput input) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pagamento non trovato: " + id)
                );

        payment.setOrderId(input.getOrderId());
        payment.setImporto(input.getImporto());
        payment.setMetodoPagamento(input.getMetodoPagamento());

        PaymentStatus paymentStatus = paymentProcessor.processPayment(input.getImporto());

        payment.setStato(paymentStatus);

        Payment updatedPayment = paymentRepository.save(payment);

        return convertToDTO(updatedPayment);
    }
    @Transactional
    public void deletePayment(UUID id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Pagamento non trovato: " + id)
                );

        paymentRepository.delete(payment);
    }

    private PaymentDTOOutput convertToDTO(Payment payment) {

        return PaymentDTOOutput.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .importo(payment.getImporto())
                .metodoPagamento(payment.getMetodoPagamento())
                .stato(payment.getStato())
                .dataCreazione(payment.getDataCreazione())
                .build();
    }
}
