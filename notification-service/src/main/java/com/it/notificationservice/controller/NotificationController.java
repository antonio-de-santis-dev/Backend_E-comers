package com.it.notificationservice.controller;

import com.it.notificationservice.dto.NotificationDTOInput;
import com.it.notificationservice.dto.NotificationDTOOutput;
import com.it.notificationservice.servis.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/{id}")
    public ResponseEntity<NotificationDTOOutput>
    updateNotification(
            @PathVariable UUID id,
            @Valid @RequestBody NotificationDTOInput input) {

        NotificationDTOOutput notification =
                notificationService.updateNotification(id, input);

        return ResponseEntity.ok(notification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable UUID id) {

        notificationService.deleteNotification(id);

        return ResponseEntity.noContent().build();
    }
}