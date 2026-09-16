package com.it.userservis.servis;


import com.it.userservis.dto.LoginDTOInuput;
import com.it.userservis.dto.LoginDTOOutput;
import com.it.userservis.entity.UserAccount;
import com.it.userservis.exception.AccountDisabledException;
import com.it.userservis.exception.InvalidCredentialsException;
import com.it.userservis.repository.UserAccountRepository;
import com.it.userservis.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginDTOOutput login(LoginDTOInuput input){

        UserAccount account = userAccountRepository
                .findByUsername(input.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Credenziali non valide"));

        if (Boolean.TRUE.equals(account.getCancelazioneRichiesta())){
            throw new AccountDisabledException(
                    "L'Account non e piu abilitato"
            );
        }

        boolean passwordCorretta = passwordEncoder.matches(
                input.getPassword(),
                account.getPassword()
        );

        if (!passwordCorretta){
            throw new InvalidCredentialsException("Credenziali non valide");
        }

        String token = jwtService.generatoreToken(
                account.getUsername()
        );

        return LoginDTOOutput.builder()
                .accountId(account.getId())
                .userId(account.getUser().getId())
                .username(account.getUsername())
                .role(account.getRole())
                .token(token)
                .build();
    }
}
