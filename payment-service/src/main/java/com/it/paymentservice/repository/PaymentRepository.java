package com.it.paymentservice.repository;

import com.it.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByCancelazioneRichiestaFalse();

    Optional<Payment> findByIdAndCancelazioneRichiestaFalse(UUID id);
}
