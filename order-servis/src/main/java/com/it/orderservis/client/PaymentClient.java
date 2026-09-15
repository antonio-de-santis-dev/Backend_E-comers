package com.it.orderservis.client;

import com.it.orderservis.dto.PaymentDTOInput;
import com.it.orderservis.dto.PaymentDTOOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.it.orderservis.exception.ExternalServiceUnavailableException;
import org.springframework.web.client.ResourceAccessException;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestClient restClient;

    @Value("${payment-service.url}")
    private String paymentServiceUrl;

    public PaymentDTOOutput createPayment(PaymentDTOInput input) {

        try {

            return restClient
                    .post()
                    .uri(paymentServiceUrl + "/api/payments")
                    .body(input)
                    .retrieve()
                    .body(PaymentDTOOutput.class);

        } catch (ResourceAccessException ex) {

            throw new ExternalServiceUnavailableException(
                    "Payment Service temporaneamente non disponibile",
                    ex
            );
        }
    }

}
