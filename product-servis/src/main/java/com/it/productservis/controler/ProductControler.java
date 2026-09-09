package com.it.productservis.controler;

import com.it.productservis.dto.ProductAvailabilityDTO;
import com.it.productservis.dto.ProductDTOInput;
import com.it.productservis.dto.ProductDTOOutput;
import com.it.productservis.dto.StockUpdateDTO;
import com.it.productservis.entity.Product;
import com.it.productservis.servis.ProductServis;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductControler {

    private final ProductServis productServis;

    @PostMapping
    public ResponseEntity<ProductDTOOutput> create(@Valid @RequestBody ProductDTOInput dto){
        ProductDTOOutput product = productServis.saved(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductDTOOutput>> findAll(){
        return ResponseEntity.ok(
                productServis.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTOOutput>  findById(@PathVariable UUID id){
        return ResponseEntity.ok(
                productServis.findById(id)
        );
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ProductAvailabilityDTO> findeByIdXcheckAvailability( @PathVariable UUID id){
        return ResponseEntity.ok(
                productServis.findeByIdXcheckAvailability(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTOOutput> update(@PathVariable UUID id, @Valid @RequestBody ProductDTOInput dto){
        return ResponseEntity.ok(
                productServis.update(id,dto)
        );
    }

    @PatchMapping("/{id}/stock/decrease")
    public ResponseEntity<ProductDTOOutput> decreaseStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockUpdateDTO input) {

        ProductDTOOutput product =
                productServis.decreaseStock(id, input.getQuantita());

        return ResponseEntity.ok(product);
    }

    @PatchMapping("/{id}/stock/increase")
    public ResponseEntity<ProductDTOOutput> increaseStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockUpdateDTO input) {

        ProductDTOOutput product =
                productServis.increaseStock(id, input.getQuantita());

        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        productServis.delete(id);
        return ResponseEntity.noContent().build();
    }

}

