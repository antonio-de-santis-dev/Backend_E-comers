package com.it.userservis.servis;

import com.it.userservis.dto.UserAccountDTOInput;
import com.it.userservis.dto.UserAccountDTOOutput;
import com.it.userservis.entity.Role;
import com.it.userservis.entity.User;
import com.it.userservis.entity.UserAccount;
import com.it.userservis.exception.ResourceAlreadyExistsException;
import com.it.userservis.exception.ResourceNotFoundException;
import com.it.userservis.repository.UserAccountRepository;
import com.it.userservis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountServis {

    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserAccountDTOOutput creaAccount(UserAccountDTOInput input){

        User user = userRepository.findById(input.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utente con id [" + input.getUserId() + "] non trovato"
                        )
                );

        if (userAccountRepository.existsByUsername(input.getUsername())){
            throw new ResourceAlreadyExistsException(
                    "Username gai esistente"
            );
        }

        UserAccount account = UserAccount.builder()
                .username(input.getUsername())
                .password(input.getPassword())
                .role(Role.USER)
                .user(user)
                .build();

        UserAccount savedAccount = userAccountRepository.save(account);

        return convertToDTO(savedAccount);
    }

    public  UserAccountDTOOutput findById(UUID id){

        UserAccount account = userAccountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account con id " + id + " non trovato"
                        ));
        return convertToDTO(account);
    }

    public UserAccountDTOOutput findByUsername(String username){

        UserAccount account = userAccountRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account con username " + username + " non trovato"
                        ));
        return convertToDTO(account);
    }

    public List<UserAccountDTOOutput> findAll(){
        return userAccountRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    private UserAccountDTOOutput convertToDTO(UserAccount account) {

        return UserAccountDTOOutput.builder()
                .id(account.getId())
                .userId(account.getUser().getId())
                .username(account.getUsername())
                .role(account.getRole())
                .build();
    }

}
