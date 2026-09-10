package com.it.orderservis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingDTOInput {

    @NotBlank
    @Email
    private String emailContatto;

    @NotBlank
    private String indirizzoSpedizione;

    @NotBlank
    private String cap;

    @NotBlank
    private String citta;

    @NotBlank
    private String provincia;

    @NotBlank
    private String regione;

    @NotBlank
    private String paese;
}