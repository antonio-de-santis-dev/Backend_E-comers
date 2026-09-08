package com.it.userservis.dto;

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
public class UserDTOInput {

    @NotBlank(message = "campo obbligatorio")
    private String nome;

    @NotBlank(message = "campo obbligatorio")
    private String cognome;

    @NotBlank(message = "campo obbligatorio")
    @Email(message = "L'email non è valida")
    private String email;

    private String indirizzo;

}
