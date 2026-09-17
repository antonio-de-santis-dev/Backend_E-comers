package com.it.userservis.configuration;

import com.it.userservis.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.it.userservis.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        //PUBBLICI GUEST
                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/users",
                                "/api/users/guest"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/user-accounts"
                        ).permitAll()

                        //SOLO ADMIN
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/users/admin/**"
                        ).hasRole("ADMIN")

                        //USER oppure ADMIN autenticati
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/users/cancellazione-request/{id}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/user-accounts/{id}",
                                "/api/user-accounts/user/{username}"
                        ).authenticated()

                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(
                                (request, response, authException) -> {

                                    response.setStatus(
                                            HttpStatus.UNAUTHORIZED.value()
                                    );

                                    response.setContentType(
                                            MediaType.APPLICATION_JSON_VALUE
                                    );

                                    ErrorResponse error =
                                            new ErrorResponse(
                                                    LocalDateTime.now(),
                                                    HttpStatus.UNAUTHORIZED.value(),
                                                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                                    "Autenticazione richiesta",
                                                    request.getRequestURI()
                                            );

                                    objectMapper.writeValue(
                                            response.getOutputStream(),
                                            error
                                    );
                                }
                        )

                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {

                                    response.setStatus(
                                            HttpStatus.FORBIDDEN.value()
                                    );

                                    response.setContentType(
                                            MediaType.APPLICATION_JSON_VALUE
                                    );

                                    ErrorResponse error =
                                            new ErrorResponse(
                                                    LocalDateTime.now(),
                                                    HttpStatus.FORBIDDEN.value(),
                                                    HttpStatus.FORBIDDEN.getReasonPhrase(),
                                                    "Accesso non consentito",
                                                    request.getRequestURI()
                                            );

                                    objectMapper.writeValue(
                                            response.getOutputStream(),
                                            error
                                    );
                                }
                        )
                )

                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}
