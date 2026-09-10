package com.it.orderservis.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestOrderDTOInput {

    @NotNull
    @Valid
    private GuestUserDTOInput user;

    @NotEmpty
    @Valid
    private List<OrderItemDTOInput> items;

    @NotNull
    private String metodoPagamento;

    @Valid
    private ShippingDTOInput shipping;

}
