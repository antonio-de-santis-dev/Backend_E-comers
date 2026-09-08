package com.it.productservis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTOInput {

    @NotBlank
    private String nome;

    @NotBlank
    private String descrizione;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal prezzo;

    @NotNull
    @Min(0)
    private Integer quantita;

    @NotBlank
    private String categoria;

    @NotNull
    private Boolean disponibile;
}
