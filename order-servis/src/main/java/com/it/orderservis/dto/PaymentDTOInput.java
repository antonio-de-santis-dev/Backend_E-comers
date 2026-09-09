package com.it.orderservis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTOInput {

    @NotNull (message = "campo obbligatorio")
    private UUID orderId;

    @NotNull (message = "campo obbligatorio")
    @DecimalMin(value = "0.01", message = "L'importo deve essere maggiore di zero")
    private BigDecimal importo;

    @NotBlank (message = "campo obbligatorio")
    private String metodoPagamento;
}
