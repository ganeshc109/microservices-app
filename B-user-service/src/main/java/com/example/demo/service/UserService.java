package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    /**
     * Create a user and put the saved user into cache under key = saved.id
     */
    @CachePut(value = "users", key = "#result.id")
    public User createUser(User user) {
        User saved = userRepository.save(user);
        log.info("DB → created user id={}", saved.getId());
        return saved;
    }

    public List<User> getAllUsers() {
        // don't cache list for now — easier invalidation when single-entity caches are used
        log.info("DB → fetching all users");
        return userRepository.findAll();
    }

    /**
     * Cache reads by user id. First call -> DB + cache write.
     * Subsequent calls -> returned from cache (no DB log).
     */
    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        log.info("DB → fetching user id={}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    /**
     * Update → write through to DB and update cache entry for this id.
     */
    @CachePut(value = "users", key = "#user.id")
    public User updateUser(User user) {
        User updated = userRepository.save(user);
        log.info("DB → updated user id={}", updated.getId());
        return updated;
    }

    /**
     * Delete → remove from DB and evict cache.
     */
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.info("DB → deleted user id={}", id);
    }
}
