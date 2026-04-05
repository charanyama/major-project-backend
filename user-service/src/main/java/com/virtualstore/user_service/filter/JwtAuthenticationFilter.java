package com.virtualstore.user_service.filter;

import com.virtualstore.user_service.repository.InvalidatedTokenRepository;
import com.virtualstore.user_service.security.UserDetailsServiceImpl;
import com.virtualstore.user_service.service.JwtService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 *
 * Runs once per request. Validates the Bearer token and populates
 * the SecurityContext so @AuthenticationPrincipal works in controllers.
 *
 * Blocklist check:
 * Before accepting the token, we look up its JTI in the
 * InvalidatedToken collection. If found → 401, even if signature is valid.
 * This is how signout works in a stateless JWT system.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // ── No token → skip (SecurityConfig will reject if path is protected) ─
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        // ── Parse + validate token ────────────────────────────────────────────
        Claims claims = jwtService.parseToken(token).orElse(null);
        if (claims == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── Blocklist check ───────────────────────────────────────────────────
        String jti = claims.getId();
        if (jti != null && invalidatedTokenRepository.existsByJti(jti)) {
            log.warn("Rejected blocklisted token jti={}", jti);
            filterChain.doFilter(request, response);
            return;
        }

        // ── Load UserDetails and set SecurityContext ───────────────────────────
        String email = claims.get("email", String.class);
        if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (userDetails.isEnabled()) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // single authority from Role enum
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}