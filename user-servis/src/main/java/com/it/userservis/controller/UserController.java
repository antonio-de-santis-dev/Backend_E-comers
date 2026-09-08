package com.it.userservis.controller;

import com.it.userservis.dto.UserDTOInput;
import com.it.userservis.entity.User;
import com.it.userservis.servis.UserServis;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserServis userServis;

    @PostMapping
    public User create(@Valid @RequestBody UserDTOInput dto){
        return userServis.saved(dto);
    }

    @GetMapping
    public List<User> findAll(){
        return userServis.findAll();
    }

    @GetMapping("/{id}")
    public User findById (@PathVariable UUID id){
        return userServis.findById(id);
    }

    @PutMapping("/{id}")
    public User update (@PathVariable UUID id , @Valid @RequestBody UserDTOInput dto){
        return userServis.update(id,dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id){
        userServis.delete(id);
    }

}
