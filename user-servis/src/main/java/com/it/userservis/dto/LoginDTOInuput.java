package com.it.userservis.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTOInuput {

    @NotBlank(message = "Username obbligatorio")
    private String username;

    @NotBlank(message = "Password obbligatoria")
    private String password;
}
