package com.it.orderservis.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTOInput {

    @NotNull (message = "campo obbligatorio")
    private UUID productId;

    @NotNull (message = "campo obbligatorio")
    @Min(value = 1,message = "la quantia deve essere almeno 1")
    private Integer quantita;
}