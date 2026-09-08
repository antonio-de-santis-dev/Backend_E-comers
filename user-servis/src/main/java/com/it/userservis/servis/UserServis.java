package com.it.userservis.servis;

import com.it.userservis.dto.UserDTOInput;
import com.it.userservis.entity.User;
import com.it.userservis.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServis {

    private final UserRepository userRepository;


    public User saved(UserDTOInput dto) {
        User user = User.builder()
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .email(dto.getEmail())
                .indirizzo(dto.getIndirizzo())
                .build();
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Id User non trovato"));

    }

    public User update(UUID id,UserDTOInput dto) {
        User user = findById(id);

        user.setNome(dto.getNome());
        user.setCognome(dto.getCognome());
        user.setEmail(dto.getEmail());
        user.setIndirizzo(dto.getIndirizzo());

        return userRepository.save(user);
    }

    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
