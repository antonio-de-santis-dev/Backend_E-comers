package com.it.orderservis.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestUserDTOInput {


    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;

    @NotBlank
    @Email
    private String email;

    @Pattern(regexp = "^[0-9+ ]{7,20}$")
    private String telefono;

    @NotBlank
    private String indirizzoResidenza;

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
