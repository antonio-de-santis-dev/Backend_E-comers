package com.it.userservis.controller;


import com.it.userservis.dto.UserAccountDTOInput;
import com.it.userservis.dto.UserAccountDTOOutput;
import com.it.userservis.servis.UserAccountServis;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-accounts")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountServis userAccountServis;

    @PostMapping
    public ResponseEntity<UserAccountDTOOutput> createAccount(@Valid @RequestBody UserAccountDTOInput input){

        UserAccountDTOOutput account = userAccountServis.creaAccount(input);

        return ResponseEntity.status(HttpStatus.CREATED).body(account);

    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAccountDTOOutput> findByID(@PathVariable UUID id){
        return ResponseEntity.ok(userAccountServis.findById(id));
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserAccountDTOOutput> findByUsername (@PathVariable String username){
        return ResponseEntity.ok(userAccountServis.findByUsername(username));
    }

}
