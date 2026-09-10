package com.it.orderservis.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTOInput {

    @NotNull(message = "campo obbligatorio")
    private UUID userId;

    @NotBlank
    private String metodoPagamento;

    @NotEmpty(message = "campo obbligatorio")
    private List<@Valid OrderItemDTOInput> items;

    @Valid
    private ShippingDTOInput shipping;
}
