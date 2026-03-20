package com.digitalearn.npaxis.token;

import org.springframework.transaction.annotation.Transactional;

public interface TokenService {

    String generateAndSaveToken(String email);

    @Transactional
    Boolean verifyToken(String email, String otp);
}
