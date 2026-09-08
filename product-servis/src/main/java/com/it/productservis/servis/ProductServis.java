package com.it.productservis.servis;

import com.it.productservis.dto.ProductDTOInput;
import com.it.productservis.entity.Product;
import com.it.productservis.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServis {

    private final ProductRepository prouctRepository;

    public Product saved(ProductDTOInput dto) {

        Product product = Product.builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .prezzo(dto.getPrezzo())
                .quantita(dto.getQuantita())
                .categoria(dto.getCategoria())
                .disponibile(dto.getDisponibile())
                .build();
        return prouctRepository.save(product);
    }

    public List<Product> findAll() {
        return prouctRepository.findAll();
    }

    public Product findById(UUID id) {
        return prouctRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prodotto non trovato"));
    }

    public Product update(UUID id,ProductDTOInput dto) {
        Product product = findById(id);

        product.setNome(dto.getNome());
        product.setDescrizione(dto.getDescrizione());
        product.setPrezzo(dto.getPrezzo());
        product.setQuantita(dto.getQuantita());
        product.setCategoria(dto.getCategoria());
        product.setDisponibile(dto.getDisponibile());

        return prouctRepository.save(product);
    }

    public void delete(UUID id) {
        prouctRepository.deleteById(id);
    }
}