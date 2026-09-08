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

    @NotBlank(message = "campo obligatorio obbligatorio")
    private String nome;

    @NotBlank(message = "campo obligatorio obbligatorio")
    private String descrizione;

    @NotNull (message = "campo obligatorio obbligatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal prezzo;

    @NotNull(message = "campo obligatorio obbligatorio")
    @Min(0)
    private Integer quantita;

    @NotBlank(message = "campo obligatorio obbligatorio")
    private String categoria;

    @NotNull(message = "campo obligatorio obbligatorio")
    private Boolean disponibile;
}
