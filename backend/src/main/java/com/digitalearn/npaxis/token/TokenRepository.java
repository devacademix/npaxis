package com.digitalearn.npaxis.token;

import com.digitalearn.npaxis.auditing.BaseRepository;

import java.util.Optional;

public interface TokenRepository extends BaseRepository<Token, Long> {
    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Token> findTopByEmailOrderByCreatedAtDesc(String email);

    Optional<Token> findByEmail(String email);
}
