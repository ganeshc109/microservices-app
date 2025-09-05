package com.example.demo.config;

import com.example.demo.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        log.info("🔹 Incoming request: {} {}", request.getMethod(), request.getRequestURI());

        if (authHeader == null) {
            log.warn("⚠️ No Authorization header found");
        } else if (!authHeader.startsWith("Bearer ")) {
            log.warn("⚠️ Authorization header does not start with 'Bearer '");
        } else {
            String token = authHeader.substring(7);
            log.info("🔑 JWT token received: {}", token);

            try {
                if (jwtUtils.validateToken(token)) {
                    Claims claims = jwtUtils.parseToken(token);
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);
                    String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority(authority))
                            );

                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.info("✅ JWT validated. User: {}, Role: {}", username, authority);
                } else {
                    log.error("❌ JWT validation failed");
                }
            } catch (Exception e) {
                log.error("❌ Exception during JWT parsing/validation: {}", e.getMessage(), e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
