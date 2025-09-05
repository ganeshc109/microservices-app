package com.example.demo.service;

import com.example.demo.model.UserEntity;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserEntity register(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
        throw new RuntimeException("Username already exists: " + username);
        }
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword)) // encrypt password
                .role(role)
                .build();
        return userRepository.save(user);
    }
}

