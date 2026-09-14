package com.it.notificationservice.repository;


import com.it.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByDataCreazioneDesc(UUID userId);

}