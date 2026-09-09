package com.it.orderservis.controler;

import com.it.orderservis.client.PaymentClient;
import com.it.orderservis.dto.PaymentDTOInput;
import com.it.orderservis.dto.PaymentDTOOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/payment")
@RequiredArgsConstructor
public class PaymentTestController {

    private final PaymentClient paymentClient;

    @PostMapping
    public ResponseEntity<PaymentDTOOutput> testPayment(
            @RequestBody PaymentDTOInput input) {

        PaymentDTOOutput response = paymentClient.createPayment(input);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}