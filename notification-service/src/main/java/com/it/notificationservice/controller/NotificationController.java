package com.it.notificationservice.controller;

import com.it.notificationservice.dto.NotificationDTOInput;
import com.it.notificationservice.dto.NotificationDTOOutput;
import com.it.notificationservice.servis.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationDTOOutput> createNotification(
            @Valid @RequestBody NotificationDTOInput input) {

        NotificationDTOOutput notification =
                notificationService.createNotification(input);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notification);
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTOOutput>> getAllNotifications() {

        List<NotificationDTOOutput> notifications =
                notificationService.getAllNotifications();

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTOOutput>
    getNotificationById(@PathVariable UUID id) {

        NotificationDTOOutput notification =
                notificationService.getNotificationById(id);

        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTOOutput>> getNotificationsByUserId(@PathVariable UUID userId){
        return ResponseEntity.ok(
                notificationService.getNotificationsByUserId(userId)
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<NotificationDTOOutput>> getNotificationsByOrderId(@PathVariable UUID orderId){
        return ResponseEntity.ok(
                notificationService.getNotificationsByOrderId(orderId)
        );
    }

    @GetMapping("/admin")
    public ResponseEntity<List<NotificationDTOOutput>> getAllNotificationsAdmin() {
        return ResponseEntity.ok(
                notificationService.getAllNotificationsAdmin()
        );
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<NotificationDTOOutput> getNotificationByIdAdmin(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationByIdAdmin(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTOOutput>
    updateNotification(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationDTOInput input) {

        NotificationDTOOutput notification =
                notificationService.updateNotification(id, input);

        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/cancellazione-request/{id}")
    public ResponseEntity<NotificationDTOOutput> richiediCancellazione(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                notificationService.richiestaCancellazione(id)
        );
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID id) {

        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }
}