package com.digitalearn.npaxis.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

/**
 * Filter to handle JWT authentication for incoming requests.
 * Validates the JWT, extracts the username, and sets the authentication context.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Intercepts incoming HTTP requests to perform JWT validation and set authentication context.
     *
     * @param request     the incoming HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to proceed with the next filters
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.debug("JwtAuthFilter: Incoming request - URI: {}, Method: {}", request.getRequestURI(), request.getMethod());

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("JwtAuthFilter: No valid Authorization header found. Skipping authentication.");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            final String username = jwtService.extractUsernameFromToken(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.info("Authorities: {}", userDetails.getAuthorities());
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("JwtAuthFilter: Authentication successful for user '{}'", username);
                } else {
                    log.warn("JwtAuthFilter: Invalid token for user '{}'", username);
                    throw new BadCredentialsException("Invalid token");
                }
            }
            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.error("JwtAuthFilter: Authentication error - {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
            return;
        } catch (Exception e) {
            log.error("JwtAuthFilter: Unexpected error - {}", e.getMessage(), e);
            handlerExceptionResolver.resolveException(request, response, null, e);
            return;
        }

        log.trace("JwtAuthFilter: Filter processing completed.");
    }

}
