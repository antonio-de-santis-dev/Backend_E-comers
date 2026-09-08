package com.it.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTOInput {

    @NotNull(message = "campo obligatorio obbligatorio")
    private UUID userId;

    @NotNull(message = "campo obligatorio obbligatorio")
    private UUID orderId;

    @NotBlank(message = "campo obligatorio obbligatorio")
    private String tipo;

    @NotBlank(message = "campo obligatorio obbligatorio")
    private String messaggio;
}