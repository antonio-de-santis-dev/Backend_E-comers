package com.it.orderservis.dto.dto;

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
public class OrderItemDTOOutput {

    private UUID id;

    private UUID productId;

    private Integer quantita;

    private BigDecimal prezzo;

}
