package com.digitalearn.npaxis.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for generating and validating JWT tokens.
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Secret key for signing the JWT tokens.
     */
    @Value("${security.jwt.secret}")
    private String SECRET_KEY;

    /**
     * Expiration time for access tokens in milliseconds.
     */
    @Value("${security.jwt.expiration.access}")
    private long ACCESS_TOKEN_VALIDITY;

    /**
     * Expiration time for refresh tokens in milliseconds.
     */
    @Value("${security.jwt.expiration.refresh}")
    private long REFRESH_TOKEN_VALIDITY;

    /**
     * Generates an access token for the given user.
     *
     * @param userDetails the user details
     * @return a signed JWT access token
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates an access token with additional claims.
     *
     * @param claims      custom claims
     * @param userDetails the user details
     * @return a signed JWT access token
     */
    public String generateAccessToken(Map<String, Object> claims, UserDetails userDetails) {
        claims.put("token-type", "access");
        log.info("Generating access token for user: {}", userDetails.getUsername());
        return buildAccessToken(claims, userDetails, ACCESS_TOKEN_VALIDITY);
    }

    /**
     * Generates a refresh token for the given user.
     *
     * @param userDetails the user details
     * @return a signed JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateRefreshToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a refresh token with additional claims.
     *
     * @param claims      custom claims
     * @param userDetails the user details
     * @return a signed JWT refresh token
     */
    public String generateRefreshToken(Map<String, Object> claims, UserDetails userDetails) {
        claims.put("token-type", "refresh");
        log.info("Generating refresh token for user: {}", userDetails.getUsername());
        return buildRefreshToken(claims, userDetails, REFRESH_TOKEN_VALIDITY);
    }

    /**
     * Builds the JWT token with claims and expiration.
     *
     * @param extraClaims    additional claims to include
     * @param userDetails    the user details
     * @param expirationTime expiration duration in milliseconds
     * @return signed JWT token string
     */
    private String buildAccessToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        log.debug("Generating Access JWT token for user: {}", userDetails.getUsername());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("authorities", authorities)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Builds the JWT token with claims and expiration.
     *
     * @param extraClaims    additional claims to include
     * @param userDetails    the user details
     * @param expirationTime expiration duration in milliseconds
     * @return signed JWT token string
     */
    private String buildRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {

        log.debug("Generating Refresh JWT token for user: {}", userDetails.getUsername());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * Retrieves the secret signing key from the application configuration.
     *
     * @return SecretKey object
     */
    private SecretKey getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Failed to decode JWT secret key: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT secret key", e);
        }
    }

    /**
     * Validates a JWT token against the provided user details.
     *
     * @param token       the JWT token
     * @param userDetails user details for comparison
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsernameFromToken(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            log.info("Token validation result for user {}: {}", username, isValid);
            return isValid;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(String token) {
        try {
            return extractExpirationFromToken(token).before(new Date());
        } catch (JwtException e) {
            log.warn("Token expiration check failed: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Extracts expiration date from the token.
     *
     * @param token the JWT token
     * @return expiration date
     */
    private Date extractExpirationFromToken(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    /**
     * Extracts the username (subject) from the token.
     *
     * @param token the JWT token
     * @return username
     */
    public String extractUsernameFromToken(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     *
     * @param token          the JWT token
     * @param claimsResolver a function to extract a claim from the Claims object
     * @param <T>            the return type
     * @return extracted claim
     */
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (ExpiredJwtException e) {
            log.warn("Token has expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            log.error("Invalid JWT: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Parses all claims from the token.
     *
     * @param token the JWT token
     * @return Claims object
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
