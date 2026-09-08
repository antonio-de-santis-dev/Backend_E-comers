package com.it.userservis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;

@Column(nullable = false)
private String nome;

@Column(nullable = false)
private String cognome;

@Column(nullable = false, unique = true)
private String email;

private String indirizzo;


}
