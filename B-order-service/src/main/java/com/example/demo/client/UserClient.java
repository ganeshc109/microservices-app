package com.example.demo.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.config.FeignConfig;
import com.example.demo.dto.UserResponse;

@FeignClient(
    name = "B-USER-SERVICE",
    configuration = FeignConfig.class,
    fallback = UserClient.UserClientFallback.class
)
public interface UserClient {
    
    @GetMapping("/api/users")
    List<UserResponse> getAllUsers();

    class UserClientFallback implements UserClient {
        @Override
        public List<UserResponse> getAllUsers() {
            // Fallback logic: return an empty list or cached data
            return List.of(); // Return an empty list as a fallback
        }
    }
}
