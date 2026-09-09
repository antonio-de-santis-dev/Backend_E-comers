package com.it.paymentservice.dto;

import com.it.paymentservice.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTOOutput {

    private UUID id;
    private UUID orderId;
    private BigDecimal importo;
    private String metodoPagamento;
    private PaymentStatus stato;
    private LocalDateTime dataCreazione;

}
