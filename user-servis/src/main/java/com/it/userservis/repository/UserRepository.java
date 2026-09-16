package com.it.userservis.repository;

import com.it.userservis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByCancelazioneRichiestaFalse();

    Optional<User> findByIdAndCancelazioneRichiestaFalse(UUID id);

}
