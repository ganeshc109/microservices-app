package com.example.demo.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Collections;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.redisson.api.RLock;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UserResponse;
import com.example.demo.model.Order;
import com.example.demo.repository.OrderRepository;
import com.example.demo.client.UserClient;

import com.netflix.discovery.EurekaClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import com.netflix.appinfo.InstanceInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final EurekaClient eurekaClient; 
    private final StringRedisTemplate redisTemplate; // ✅ used for distributed lock

    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "userService")
    public Order createOrder(Order order) {
        if (eurekaClient.getApplication("B-USER-SERVICE") == null) {
            log.error("❌ B-USER-SERVICE not yet discovered in Eureka. Aborting Feign call.");
            throw new IllegalStateException("User Service not available in Eureka");
        } else {
            List<InstanceInfo> instances = eurekaClient.getApplication("B-USER-SERVICE").getInstances();
            log.info("✅ Found {} instance(s) of B-USER-SERVICE in Eureka: {}", instances.size(), instances);
        }

        List<UserResponse> users = userClient.getAllUsers();
        if (users.isEmpty()) {
            log.warn("⚠️ Feign call succeeded, but returned no users.");
            throw new RuntimeException("No users available to assign order");
        }

        Order savedOrder = orderRepository.save(order);
        log.info("✅ Order saved successfully with id={}", savedOrder.getId());

        return savedOrder;
    }

    // 🔒 Improved distributed lock with UUID token + Lua release
    public Order createOrderWithLock(Order order) {
    String lockKey = "order-lock:" + order.getProductName();
    long lockTtlSeconds = 10L;

    // Try to acquire lock
    Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "LOCKED", lockTtlSeconds, TimeUnit.SECONDS);

    if (!Boolean.TRUE.equals(acquired)) {
        log.warn("⚠️ Order for '{}' is already in progress", order.getProductName());
        throw new DuplicateOrderException("Order for '" + order.getProductName() + "' is already in progress");
    }

    log.info("🔒 Lock acquired for {}", order.getProductName());

    // Proceed immediately
    return createOrder(order);

    // No manual release — TTL will automatically remove the key after 10s
}




    // --- CircuitBreaker fallback
    public Order getAllUsersFallback(Order order, Throwable t) {
        log.error("❌ Fallback triggered for createOrder due to: {}", t.getMessage());
        Order fallbackOrder = new Order();
        fallbackOrder.setId(-1L);
        fallbackOrder.setProductName("Fallback Item");
        fallbackOrder.setQuantity(0);
        fallbackOrder.setPrice(0.0);
        return fallbackOrder;
    }

    // ✅ Cache directly the entity, not Optional<Order>
    @Cacheable(value = "orders", key = "#id")
    public Order getOrderById(Long id) {
        log.info("DB 🔍 fetching order id={}", id);
        return orderRepository.findById(id).orElse(null);
    }

    @CachePut(value = "orders", key = "#order.id")
    public Order updateOrder(Order order) {
        log.info("DB 📝 updating order {}", order);
        return orderRepository.save(order);
    }

    @CacheEvict(value = "orders", key = "#id")
    public void deleteOrder(Long id) {
        log.info("DB ❌ deleting order id={}", id);
        orderRepository.deleteById(id);
    }

    public List<Order> getAllOrders() {
        log.info("DB 🔍 fetching all orders");
        return orderRepository.findAll();
    }
}
