package com.it.userservis.controller;

import com.it.userservis.dto.UserDTOInput;
import com.it.userservis.dto.UserDTOOutput;
import com.it.userservis.entity.User;
import com.it.userservis.servis.UserServis;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServis userServis;

    @PostMapping
    public ResponseEntity<UserDTOOutput> create(@Valid @RequestBody UserDTOInput dto){

        UserDTOOutput user = userServis.saved(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDTOOutput>> findAll(){
        return ResponseEntity.ok(
                userServis.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTOOutput> findById (@PathVariable UUID id){

        return ResponseEntity.ok(
                userServis.findById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTOOutput>  update (@PathVariable UUID id , @Valid @RequestBody UserDTOInput dto){
        return ResponseEntity.ok(
                userServis.update(id,dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        userServis.delete(id);
        return ResponseEntity.noContent().build();
    }

}
