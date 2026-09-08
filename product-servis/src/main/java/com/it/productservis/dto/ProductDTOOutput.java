package com.it.productservis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTOOutput {

    private UUID id;

    private String nome;

    private String descrizione;

    private BigDecimal prezzo;

    private Integer quantita;

    private String categoria;

    private Boolean disponibile;
}
