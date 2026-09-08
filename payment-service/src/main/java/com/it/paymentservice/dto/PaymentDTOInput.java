package com.it.paymentservice.dto;

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

    @NotNull
    private UUID orderId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal importo;

    @NotBlank
    private String metodoPagamento;
}
