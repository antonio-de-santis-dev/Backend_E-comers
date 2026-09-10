package com.it.userservis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTOOutput {

    private UUID id;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    private String indirizzoResidenza;
    private String indirizzoSpedizione;
    private String cap;
    private String citta;
    private String provincia;
    private String regione;
    private String paese;

}
