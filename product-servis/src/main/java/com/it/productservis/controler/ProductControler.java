package com.it.productservis.controler;

import com.it.productservis.dto.ProductDTOInput;
import com.it.productservis.entity.Product;
import com.it.productservis.servis.ProductServis;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductControler {

    private final ProductServis productServis;

    @PostMapping
    public Product create(@Valid @RequestBody ProductDTOInput dto){
        return productServis.saved(dto);
    }

    @GetMapping
    private List<Product> findAll(){
        return productServis.findAll();
    }

    @GetMapping("/{id}")
    private Product findById(@PathVariable UUID id){
        return productServis.findById(id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable UUID id, @Valid @RequestBody ProductDTOInput dto){
        return productServis.update(id,dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id){
        productServis.delete(id);
    }

}

