package com.it.paymentservice.controller;

import com.it.paymentservice.dto.PaymentDTOInput;
import com.it.paymentservice.dto.PaymentDTOOutput;
import com.it.paymentservice.servis.PaymentServis;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServis paymentService;

    @PostMapping
    public ResponseEntity<PaymentDTOOutput> createPayment(
            @Valid @RequestBody PaymentDTOInput input) {

        PaymentDTOOutput payment =
                paymentService.createPayment(input);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payment);
    }

    @GetMapping
    public ResponseEntity<List<PaymentDTOOutput>> getAllPayments() {

        List<PaymentDTOOutput> payments =
                paymentService.getAllPayments();

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTOOutput> getPaymentById(
            @PathVariable UUID id) {

        PaymentDTOOutput payment =
                paymentService.getPaymentById(id);

        return ResponseEntity.ok(payment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentDTOOutput> updatePayment(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentDTOInput input) {

        PaymentDTOOutput payment =
                paymentService.updatePayment(id, input);

        return ResponseEntity.ok(payment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(
            @PathVariable UUID id) {

        paymentService.deletePayment(id);

        return ResponseEntity.noContent().build();
    }

}
