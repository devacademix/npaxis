package com.digitalearn.npaxis.token;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Token> findTopByEmailOrderByCreatedAtDesc(String email);}
