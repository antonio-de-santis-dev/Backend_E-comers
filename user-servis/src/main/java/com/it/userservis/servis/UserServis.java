package com.it.userservis.servis;

import com.it.userservis.dto.UserDTOInput;
import com.it.userservis.dto.UserDTOOutput;
import com.it.userservis.entity.User;
import com.it.userservis.repository.UserRepository;
import com.it.userservis.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServis {

    private final UserRepository userRepository;


    public UserDTOOutput saved(UserDTOInput dto) {
        User user = User.builder()
                .nome(dto.getNome())
                .cognome(dto.getCognome())
                .email(dto.getEmail())
                .indirizzo(dto.getIndirizzo())
                .build();

        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

    public List<UserDTOOutput> findAll() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    public UserDTOOutput findById(UUID id) {
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato nel sistema  id: "+id));
        return convertToDTO(user);
    }

    public UserDTOOutput update(UUID id,UserDTOInput dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato nel sistema  id: "+id));;

        user.setNome(dto.getNome());
        user.setCognome(dto.getCognome());
        user.setEmail(dto.getEmail());
        user.setIndirizzo(dto.getIndirizzo());

        User updateUser = userRepository.save(user);

        return convertToDTO(updateUser);
    }

    public void delete(UUID id) {

        User user =  userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato nel sistema  id: "+id));

        userRepository.delete(user);
    }

    private UserDTOOutput convertToDTO(User user) {

        return UserDTOOutput.builder()
                .id(user.getId())
                .nome(user.getNome())
                .cognome(user.getCognome())
                .email(user.getEmail())
                .indirizzo(user.getIndirizzo())
                .build();
    }
}
