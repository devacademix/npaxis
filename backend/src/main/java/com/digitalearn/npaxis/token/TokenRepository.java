package com.digitalearn.npaxis.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Token> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<Token> findByEmail(String email);
}
