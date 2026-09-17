package com.it.notificationservice.repository;


import com.it.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByDataCreazioneDesc(UUID userId);

    List<Notification> findByOrderIdOrderByDataCreazioneDesc(UUID orderId);

    List<Notification> findAllByCancelazioneRichiestaFalse();

    Optional<Notification> findByIdAndCancelazioneRichiestaFalse(UUID id);

    List<Notification> findByUserIdAndCancelazioneRichiestaFalseOrderByDataCreazioneDesc(UUID userId);

    List<Notification> findByOrderIdAndCancelazioneRichiestaFalseOrderByDataCreazioneDesc(UUID orderId);

}