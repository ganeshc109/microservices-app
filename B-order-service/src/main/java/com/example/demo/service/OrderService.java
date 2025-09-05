package com.example.demo.service;

import java.util.List;
import java.util.Optional;

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
    private final EurekaClient eurekaClient; // inject Eureka client

    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    @Retry(name = "userService")
    public Order createOrder(Order order) {
        // ✅ Check Eureka registry first
        if (eurekaClient.getApplication("B-USER-SERVICE") == null) {
            log.error("❌ B-USER-SERVICE not yet discovered in Eureka. Aborting Feign call.");
            throw new IllegalStateException("User Service not available in Eureka");
        } else {
            List<InstanceInfo> instances = eurekaClient.getApplication("B-USER-SERVICE").getInstances();
            log.info("✅ Found {} instance(s) of B-USER-SERVICE in Eureka: {}", instances.size(), instances);
        }

        // ✅ Now safely call Feign
        List<UserResponse> users = userClient.getAllUsers();
        if (users.isEmpty()) {
            log.warn("⚠️ Feign call succeeded, but returned no users.");
            throw new RuntimeException("No users available to assign order");
        } else {
            log.info("✅ Feign call returned {} users. userswe: {}", users.size(), users);
        }

        // ✅ Save order
        Order savedOrder = orderRepository.save(order);
        log.info("✅ Order saved successfully with id={}", savedOrder.getId());

        return savedOrder;
    }

    // Fallback method for CircuitBreaker
    public Order getAllUsersFallback(Order order, Throwable t) {
        log.error("❌ Fallback triggered for createOrder due to: {}", t.getMessage());
        // Handle fallback logic, e.g., return a default order or throw a custom exception
        // return dummy order
        Order fallbackOrder = new Order();
        fallbackOrder.setId(-1L);
        fallbackOrder.setProductName("Fallback Item");
        fallbackOrder.setQuantity(0);
        fallbackOrder.setPrice(0.0);
        return fallbackOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
