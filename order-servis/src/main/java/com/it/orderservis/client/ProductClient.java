package com.it.orderservis.client;

import com.it.orderservis.dto.ProductDTOOutput;
import com.it.orderservis.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.it.orderservis.dto.StockUpdateDTO;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductClient {

    private final RestClient restClient;

    @Value("${product-service.url}")
    private String productServiceUrl;

    public ProductDTOOutput findProductById(UUID productId) {

        return restClient
                .get()
                .uri(productServiceUrl + "/api/products/{id}",productId)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            throw new ResourceNotFoundException(
                        "Prodotto con id : ["+productId+"] non torvato"
                                );
                            }
                )
                .body(ProductDTOOutput.class);
    }

    public ProductDTOOutput decreaseStock(UUID productId, Integer quantita){

        StockUpdateDTO input = new StockUpdateDTO(quantita);

        return restClient
                .patch()
                .uri(
                        productServiceUrl + "/api/products/{id}/stock/decrease", productId
                )
                .body(input)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (request, response) -> {
                            throw new ResourceNotFoundException(
                                    "Prodotto con id [" + productId + "] non trovato"
                                );
                            }
                        )
                .body(ProductDTOOutput.class);
    }
}
