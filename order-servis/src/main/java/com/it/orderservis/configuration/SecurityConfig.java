package com.it.orderservis.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.it.orderservis.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // GUEST
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders/guest"
                        ).permitAll()

                        // SOLO ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/orders/admin/**"
                        ).hasRole("ADMIN")

                        // USER + ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/orders"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/orders/{id}",
                                "/api/orders/cod/{codOrder}",
                                "/api/orders/user/{userId}"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/orders/cancellazione-request/{id}"
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

                                    Map<String, Object> error = Map.of(
                                            "timestamp", LocalDateTime.now().toString(),
                                            "status", 401,
                                            "error", "Unauthorized",
                                            "message", "Autenticazione richiesta",
                                            "path", request.getRequestURI()
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

                                    Map<String, Object> error = Map.of(
                                            "timestamp", LocalDateTime.now().toString(),
                                            "status", 403,
                                            "error", "Forbidden",
                                            "message", "Accesso non consentito",
                                            "path", request.getRequestURI()
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