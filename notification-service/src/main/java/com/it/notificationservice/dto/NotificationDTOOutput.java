package com.it.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTOOutput {

    private UUID id;
    private UUID userId;
    private UUID orderId;
    private String tipo;
    private String messaggio;
    private String stato;
    private LocalDateTime dataCreazione;
}