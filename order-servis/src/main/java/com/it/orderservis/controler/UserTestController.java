package com.it.orderservis.controler;

import com.it.orderservis.client.UserClient;
import com.it.orderservis.dto.UserDTOOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/test/user")
@RequiredArgsConstructor
public class UserTestController {

    private final UserClient userClient;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTOOutput> testUser(
            @PathVariable UUID id) {

        UserDTOOutput user =
                userClient.findUserById(id);

        return ResponseEntity.ok(user);
    }
}