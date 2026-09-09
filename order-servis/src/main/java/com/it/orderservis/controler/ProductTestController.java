package com.it.orderservis.controler;

import com.it.orderservis.client.ProductClient;
import com.it.orderservis.dto.ProductDTOOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/test/product")
@RequiredArgsConstructor
public class ProductTestController {

    private final ProductClient productClient;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTOOutput> testProduct(
            @PathVariable UUID id) {

        ProductDTOOutput product =
                productClient.findProductById(id);

        return ResponseEntity.ok(product);
    }
}