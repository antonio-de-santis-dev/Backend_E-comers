package com.it.userservis.servis;

import org.springframework.transaction.annotation.Transactional;
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


    @Transactional
    public UserDTOOutput saved(UserDTOInput input) {
        User user = User.builder()
                .nome(input.getNome())
                .cognome(input.getCognome())
                .email(input.getEmail())
                .telefono(input.getTelefono())
                .indirizzoResidenza(input.getIndirizzoResidenza())
                .indirizzoSpedizione(resolveIndirizzoSpedizione(input))
                .cap(input.getCap())
                .citta(input.getCitta())
                .provincia(input.getProvincia())
                .regione(input.getRegione())
                .paese(input.getPaese())
                .build();

        User savedUser = userRepository.save(user);

        return convertToDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDTOOutput> findAll() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }
    @Transactional(readOnly = true)
    public UserDTOOutput findById(UUID id) {
        User user =  userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente_non_trovato_nel_sistema_id= "+id));
        return convertToDTO(user);
    }
    @Transactional
    public UserDTOOutput update(UUID id,UserDTOInput input) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente_non_trovato_nel_sistema_id= "+id));;

        user.setNome(input.getNome());
        user.setCognome(input.getCognome());
        user.setEmail(input.getEmail());
        user.setTelefono(input.getTelefono());
        user.setIndirizzoResidenza(input.getIndirizzoResidenza());
        user.setIndirizzoSpedizione(resolveIndirizzoSpedizione(input));
        user.setCap(input.getCap());
        user.setCitta(input.getCitta());
        user.setProvincia(input.getProvincia());
        user.setRegione(input.getRegione());
        user.setPaese(input.getPaese());

        User updateUser = userRepository.save(user);

        return convertToDTO(updateUser);
    }
    @Transactional
    public void delete(UUID id) {

        User user =  userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente non trovato nel sistema id = "+id));

        userRepository.delete(user);
    }


    private String resolveIndirizzoSpedizione(UserDTOInput input){

        if (input.getIndirizzoSpedizione() == null || input.getIndirizzoSpedizione().isBlank()){
            return input.getIndirizzoResidenza();
        }
        return input.getIndirizzoSpedizione();
    }

    private UserDTOOutput convertToDTO(User user) {

        return UserDTOOutput.builder()
                .id(user.getId())
                .nome(user.getNome())
                .cognome(user.getCognome())
                .email(user.getEmail())
                .telefono(user.getTelefono())
                .indirizzoResidenza(user.getIndirizzoResidenza())
                .indirizzoSpedizione(user.getIndirizzoSpedizione())
                .cap(user.getCap())
                .citta(user.getCitta())
                .provincia(user.getProvincia())
                .regione(user.getRegione())
                .paese(user.getPaese())
                .build();
    }
}
