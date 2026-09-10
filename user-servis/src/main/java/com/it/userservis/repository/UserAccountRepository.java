package com.it.userservis.repository;

import com.it.userservis.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    //cercare un account tramite username;
    Optional<UserAccount> findByUsername(String username);

    //evitare username duplicati;
    boolean existsByUsername(String username);

    //evitare che lo stesso User abbia più account.
    boolean existsByUserId(UUID userId);
}
