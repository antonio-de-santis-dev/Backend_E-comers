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
public class UserDTOOutput {

    private UUID id;
    private String nome;
    private String cognome;
    private String email;
    private String indirizzo;

}
