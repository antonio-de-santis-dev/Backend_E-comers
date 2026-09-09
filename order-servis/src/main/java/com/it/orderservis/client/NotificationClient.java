package com.it.orderservis.client;

import com.it.orderservis.dto.NotificationDTOInput;
import com.it.orderservis.dto.NotificationDTOOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestClient restClient;

    @Value("${notification-service.url}")
    private String notificationServiceUrl;

    public NotificationDTOOutput createNotification(
            NotificationDTOInput input) {

        return restClient
                .post()
                .uri(notificationServiceUrl + "/api/notifications")
                .body(input)
                .retrieve()
                .body(NotificationDTOOutput.class);
    }
}
