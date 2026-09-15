package com.it.userservis.servis;


import com.it.userservis.dto.LoginDTOInuput;
import com.it.userservis.entity.UserAccount;
import com.it.userservis.exception.ResourceNotFoundException;
import com.it.userservis.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccount login(LoginDTOInuput input){

        UserAccount account = userAccountRepository
                .findByUsername(input.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account con username: " + input.getUsername() + "non trovato"
                ));

        boolean passwordCorretta = passwordEncoder.matches(
                input.getPassword(),
                account.getPassword()
        );

        if (!passwordCorretta){
            throw new RuntimeException("Credenziali non valide");
        }
        return account;
    }
}
