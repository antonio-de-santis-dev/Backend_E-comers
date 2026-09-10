package com.it.orderservis.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "shipping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String emailContatto;

    @Column(nullable = false)
    private String indirizzoSpedizione;

    @Column(nullable = false, length = 10)
    private String cap;

    @Column(nullable = false)
    private String citta;

    @Column(nullable = false, length = 100)
    private String provincia;

    @Column(nullable = false, length = 100)
    private String regione;

    @Column(nullable = false, length = 100)
    private String paese;

    @OneToOne(optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            unique = true
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;
}
