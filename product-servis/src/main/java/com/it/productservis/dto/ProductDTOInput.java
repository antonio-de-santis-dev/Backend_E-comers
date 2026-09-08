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

    @NotBlank(message = "campo obbligatorio")
    private String nome;

    @NotBlank(message = "campo obbligatorio")
    private String descrizione;

    @NotNull (message = "campo obbligatorio")
    @DecimalMin(value = "0.0", message = "Il prezzo non puo esere negativo")
    private BigDecimal prezzo;

    @NotNull(message = "campo obbligatorio")
    @Min(value = 0, message = "La quantità non può essere negativa")
    private Integer quantita;

    @NotBlank(message = "campo obbligatorio")
    private String categoria;

    @NotNull(message = "campo obbligatorio")
    private Boolean disponibile;
}
