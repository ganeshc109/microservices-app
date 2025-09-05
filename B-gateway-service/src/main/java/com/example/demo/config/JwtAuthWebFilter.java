package com.example.demo.config;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.example.demo.util.JwtUtil;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthWebFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info("🔹 Incoming request: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getPath());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("🔑 JWT token received");

            try {
                if (jwtUtil.validateToken(token)) {
                    Claims claims = jwtUtil.extractAllClaims(token);
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);
                    String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority(authority))
                    );

                    log.info("✅ JWT validated. User: {}, Role: {}", username, authority);

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                } else {
                    log.error("❌ JWT validation failed");
                }
            } catch (Exception e) {
                log.error("❌ Exception during JWT parsing/validation: {}", e.getMessage(), e);
            }
        } else {
            log.warn("⚠️ Missing or invalid Authorization header");
        }

        return chain.filter(exchange);
    }
}