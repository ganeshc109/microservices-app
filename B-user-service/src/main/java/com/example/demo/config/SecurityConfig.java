package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  @Order(1) // ensure this chain is evaluated before any other
  SecurityFilterChain appChain(HttpSecurity http) throws Exception {
    http
      .securityMatcher("/**") // match all requests for this chain
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
          .requestMatchers("/actuator/health", "/actuator/info").permitAll()
          .requestMatchers("/api/**").permitAll()
          .anyRequest().authenticated()
      )
      .httpBasic(Customizer.withDefaults());
    return http.build();
  }
}
