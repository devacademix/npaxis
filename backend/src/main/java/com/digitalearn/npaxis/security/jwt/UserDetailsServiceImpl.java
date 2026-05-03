package com.digitalearn.npaxis.security.jwt;

import com.digitalearn.npaxis.analytics.AnalyticsService;
import com.digitalearn.npaxis.analytics.EventType;
import com.digitalearn.npaxis.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;


/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * This service is used to load user-specific data during authentication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Repository for accessing user data.
     */
    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;

    /**
     * Loads the user by their email (used as the email in this system).
     *
     * @param email the email of the user to load
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if no user is found with the given email
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Log the attempt to load user details
        log.info("UserDetailsServiceImpl - Loading user with email: {}", email);

        UserDetails userDetails = this.userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found in repository for email: {}", email);

                    // Track failed user lookup attempt
                    Map<String, Object> failureMetadata = new HashMap<>();
                    failureMetadata.put("email", email);
                    failureMetadata.put("reason", "user_not_found");
                    analyticsService.trackBackendEvent(
                        EventType.USER_LOGIN,
                        null,
                        email,
                        failureMetadata
                    );

                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        // Track successful user details load
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("email", email);
        metadata.put("loadStatus", "success");
        analyticsService.trackBackendEvent(
            EventType.USER_LOGIN,
            null,
            email,
            metadata
        );

        return userDetails;
    }
}
