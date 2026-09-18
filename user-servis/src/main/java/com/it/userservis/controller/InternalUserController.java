package com.it.userservis.controller;

import com.it.userservis.dto.UserDTOOutput;
import com.it.userservis.servis.UserServis;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserServis userServis;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTOOutput> findUserByID(@PathVariable UUID id){

        return ResponseEntity.ok(
                userServis.findById(id)
        );
    }



}
