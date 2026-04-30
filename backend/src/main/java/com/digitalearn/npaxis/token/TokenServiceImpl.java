package com.digitalearn.npaxis.token;

import com.digitalearn.npaxis.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private static final int OTP_EXPIRATION_MINUTES = 5;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateAndSaveToken(String email) {
        String generatedOtp = this.generateOtp(6); // generate once
        Token token = this.generateToken(email, generatedOtp);
        this.tokenRepository.save(token);
        return generatedOtp; // return unhashed OTP
    }

    @Transactional
    @Override
    public Boolean verifyToken(String email, String otp) {
        log.info("Verifying otp for user with email: " + email);

        Token token = this.tokenRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ResourceNotFoundException("Otp not found for email: " + email));

        // check if already verified
        if (token.isVerified()) {
            return false;
        }
        // check expiration
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        // verify OTP (hashed comparison)
        if (!passwordEncoder.matches(otp, token.getHashedOtp())) {
            return false;
        }
        // mark as verified
        token.setVerified(true);
        token.setVerifiedAt(LocalDateTime.now());
        this.tokenRepository.save(token);
        log.info("OTP verified successfully.");
        return true;
    }

    //	convert string otp to token
    private Token generateToken(String email, String otp) {
        return Token.builder()
                .hashedOtp(passwordEncoder.encode(otp)) // store hashed
                .email(email)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES))
                .build();
    }

    //	generate otp code
    private String generateOtp(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = this.secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }
}
