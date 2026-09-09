package com.it.productservis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductAvailabilityDTO {

    private UUID id;
    private Integer quantita;
    private Boolean disponibile;

}
