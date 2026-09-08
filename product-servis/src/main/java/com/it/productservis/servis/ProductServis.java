package com.it.productservis.servis;

import com.it.productservis.dto.ProductDTOInput;
import com.it.productservis.dto.ProductDTOOutput;
import com.it.productservis.entity.Product;
import com.it.productservis.exception.ResourceNotFoundException;
import com.it.productservis.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServis {

    private final ProductRepository prouctRepository;

    @Transactional
    public ProductDTOOutput saved(ProductDTOInput dto) {

        Product product = Product.builder()
                .nome(dto.getNome())
                .descrizione(dto.getDescrizione())
                .prezzo(dto.getPrezzo())
                .quantita(dto.getQuantita())
                .categoria(dto.getCategoria())
                .disponibile(dto.getDisponibile())
                .build();
        Product savedProduct = prouctRepository.save(product);

        return convertToDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public  List<ProductDTOOutput>  findAll() {

        return prouctRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
    @Transactional(readOnly = true)
    public ProductDTOOutput findById(UUID id) {
         Product product= prouctRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato id: " + id));
        return convertToDTO(product);
    }
    @Transactional
    public ProductDTOOutput update(UUID id,ProductDTOInput dto) {
        Product product= prouctRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato id: " + id));

        product.setNome(dto.getNome());
        product.setDescrizione(dto.getDescrizione());
        product.setPrezzo(dto.getPrezzo());
        product.setQuantita(dto.getQuantita());
        product.setCategoria(dto.getCategoria());
        product.setDisponibile(dto.getDisponibile());

        Product updateProduct = prouctRepository.save(product);

        return convertToDTO(updateProduct);
    }
    @Transactional
    public void delete(UUID id) {
        Product product= prouctRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prodotto non trovato id: " + id));

        prouctRepository.delete(product);
    }

    private ProductDTOOutput convertToDTO(Product product) {

        return ProductDTOOutput.builder()
                .id(product.getId())
                .nome(product.getNome())
                .descrizione(product.getDescrizione())
                .prezzo(product.getPrezzo())
                .quantita(product.getQuantita())
                .categoria(product.getCategoria())
                .disponibile(product.getDisponibile())
                .build();
    }
}