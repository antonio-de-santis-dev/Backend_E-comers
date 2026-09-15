package com.it.userservis.controller;

import com.it.userservis.dto.LoginDTOInuput;
import com.it.userservis.dto.LoginDTOOutput;
import com.it.userservis.entity.UserAccount;
import com.it.userservis.servis.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginDTOOutput> login(@Valid @RequestBody LoginDTOInuput input){
        LoginDTOOutput account = authService.login(input);
        return ResponseEntity.ok(account);
    }
}
