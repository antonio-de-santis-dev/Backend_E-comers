package com.it.notificationservice.servis;

import com.it.notificationservice.dto.NotificationDTOInput;
import com.it.notificationservice.dto.NotificationDTOOutput;
import com.it.notificationservice.entity.Notification;
import com.it.notificationservice.exception.ResourceNotFoundException;
import com.it.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    @Transactional
    public NotificationDTOOutput createNotification( NotificationDTOInput input) {

        Notification notification = Notification.builder()
                .userId(input.getUserId())
                .orderId(input.getOrderId())
                .tipo(input.getTipo())
                .messaggio(input.getMessaggio())
                .stato("PENDING")
                .dataCreazione(LocalDateTime.now())
                .build();

        Notification savedNotification =
                notificationRepository.save(notification);

        return convertToDTO(savedNotification);
    }
    @Transactional(readOnly = true)
    public List<NotificationDTOOutput> getAllNotifications() {

        return notificationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public NotificationDTOOutput getNotificationById(UUID id) {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notifica non trovata: " + id
                                )
                        );

        return convertToDTO(notification);
    }
    @Transactional
    public NotificationDTOOutput updateNotification( UUID id, NotificationDTOInput input) {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notifica non trovata: " + id
                                )
                        );

        notification.setUserId(input.getUserId());
        notification.setOrderId(input.getOrderId());
        notification.setTipo(input.getTipo());
        notification.setMessaggio(input.getMessaggio());

        Notification updatedNotification =
                notificationRepository.save(notification);

        return convertToDTO(updatedNotification);
    }
    @Transactional
    public void deleteNotification(UUID id) {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notifica non trovata: " + id
                                )
                        );

        notificationRepository.delete(notification);
    }

    private NotificationDTOOutput convertToDTO( Notification notification) {

        return NotificationDTOOutput.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .orderId(notification.getOrderId())
                .tipo(notification.getTipo())
                .messaggio(notification.getMessaggio())
                .stato(notification.getStato())
                .dataCreazione(notification.getDataCreazione())
                .build();
    }
}