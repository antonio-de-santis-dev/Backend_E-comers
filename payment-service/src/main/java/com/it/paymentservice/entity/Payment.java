package com.it.paymentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal importo;

    @Column(nullable = false)
    private String metodoPagamento;

    @Enumerated(EnumType.STRING)
    private PaymentStatus stato;

    @Column(nullable = false)
    private LocalDateTime dataCreazione;
}
