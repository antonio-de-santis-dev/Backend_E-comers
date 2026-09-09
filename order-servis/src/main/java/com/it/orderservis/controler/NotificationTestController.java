package com.it.orderservis.controler;

import com.it.orderservis.client.NotificationClient;
import com.it.orderservis.dto.NotificationDTOInput;
import com.it.orderservis.dto.NotificationDTOOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test/notification")
@RequiredArgsConstructor
public class NotificationTestController {

    private final NotificationClient notificationClient;

    @PostMapping
    public ResponseEntity<NotificationDTOOutput> testNotification(
            @RequestBody NotificationDTOInput input) {

        NotificationDTOOutput notification =
                notificationClient.createNotification(input);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notification);
    }
}