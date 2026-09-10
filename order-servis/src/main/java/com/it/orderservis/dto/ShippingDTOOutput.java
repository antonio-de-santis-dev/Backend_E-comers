package com.it.orderservis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingDTOOutput {

    private UUID id;

    private String codOrder;

    private String emailContatto;

    private String indirizzoSpedizione;

    private String cap;

    private String citta;

    private String provincia;

    private String regione;

    private String paese;
}