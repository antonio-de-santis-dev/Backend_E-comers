package com.it.orderservis.client;

import com.it.orderservis.dto.GuestUserDTOInput;
import com.it.orderservis.dto.UserDTOOutput;
import com.it.orderservis.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.it.orderservis.exception.ExternalServiceUnavailableException;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestClient restClient;

    @Value("${user-service.url}")
    private String userServiceUrl;

    public UserDTOOutput findUserById(UUID userId) {

        try {

            return restClient
                    .get()
                    .uri(userServiceUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, response) -> {
                                throw new ResourceNotFoundException(
                                        "Utente con id (" + userId + ") non trovato"
                                );
                            }
                    )
                    .body(UserDTOOutput.class);

        } catch (ResourceAccessException ex) {

            throw new ExternalServiceUnavailableException(
                    "User Service temporaneamente non disponibile",
                    ex
            );
        }
    }

    public UserDTOOutput resolveGuest(GuestUserDTOInput input) {

        try {

            return restClient
                    .post()
                    .uri(userServiceUrl + "/api/users/guest")
                    .body(input)
                    .retrieve()
                    .body(UserDTOOutput.class);

        } catch (ResourceAccessException ex) {

            throw new ExternalServiceUnavailableException(
                    "User Service temporaneamente non disponibile",
                    ex
            );
        }
    }
}

