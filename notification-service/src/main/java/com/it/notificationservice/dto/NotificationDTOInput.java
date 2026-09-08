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

    @NotNull(message = "campo obbligatorio")
    private UUID userId;

    @NotNull(message = "campo obbligatorio")
    private UUID orderId;

    @NotBlank(message = "campo obbligatorio")
    private String tipo;

    @NotBlank(message = "campo obbligatorio")
    private String messaggio;
}