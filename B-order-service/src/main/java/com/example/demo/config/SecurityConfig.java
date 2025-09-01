package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  // In-memory user (test / test123)
  @Bean
  public UserDetailsService userDetailsService() {
    return new InMemoryUserDetailsManager(
        User.withUsername("test")
            .password("{noop}test123") // {noop} = plain text password encoder
            .roles("USER")
            .build()
    );
  }

  @Bean
  @Order(1)
  SecurityFilterChain appChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
          .requestMatchers("/actuator/health", "/actuator/info").permitAll()
          .requestMatchers("/api/**").authenticated() // <-- require auth now
          .anyRequest().authenticated()
      )
      .httpBasic(Customizer.withDefaults());
    return http.build();
  }
}
