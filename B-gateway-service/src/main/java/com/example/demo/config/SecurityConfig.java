// package com.example.demo.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
// import org.springframework.security.config.web.server.ServerHttpSecurity;
// import org.springframework.security.web.server.SecurityWebFilterChain;

// @Configuration
// @EnableWebFluxSecurity
// public class SecurityConfig {

//     @Bean
//     public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//         return http
//             .csrf(csrf -> csrf.disable())
//             .httpBasic(basic -> basic.disable())
//             .formLogin(form -> form.disable())
//             .authorizeExchange(exchanges -> exchanges
//                 .anyExchange().permitAll()  // Allow ALL requests
//             )
//             .build();
//     }
// }
