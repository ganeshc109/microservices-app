package com.example.demo.service;

import com.example.demo.dto.OrderEvent;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderConsumerService {

    private final String topicName;
    private final JwtUtil jwtUtil;

    // Inject the topic bean + JwtUtil
    public OrderConsumerService(NewTopic ordersTopic, JwtUtil jwtUtil) {
        this.topicName = ordersTopic.name();
        this.jwtUtil = jwtUtil;
    }

    public String getTopicName() {
        return topicName;
    }

    // ✅ Main consumer for "orders-topic"
    @KafkaListener(
            topics = "#{__listener.topicName}",
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(OrderEvent event) {
        log.info("📥 User-Service received Order event: {}", event);

        // 🔥 Simulate failure for testing retry + DLT
        try {
            // Extract numeric part from orderId (e.g., "O123" → 123)
            String numericPart = event.getOrderId().replaceAll("\\D+", "");
            int orderIdInt = Integer.parseInt(numericPart);

            if (orderIdInt % 2 == 0) {
                log.error("❌ Simulated processing failure for orderId {}", event.getOrderId());
                throw new RuntimeException("Simulated processing failure");
            }
        } catch (NumberFormatException e) {
            log.warn("⚠️ Cannot parse numeric part of orderId '{}', skipping simulated failure", event.getOrderId());
        }

        // 🔥 Validate JWT if present
        String jwt = event.getJwtToken();
        if (jwt != null) {
            try {
                if (jwtUtil.validateToken(jwt)) {
                    Claims claims = jwtUtil.extractAllClaims(jwt);
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);

                    log.info("✅ Valid JWT from Kafka event → User: {}, Role: {}", username, role);
                } else {
                    log.error("❌ Invalid JWT received in Kafka event");
                    throw new RuntimeException("Invalid JWT");
                }
            } catch (Exception e) {
                log.error("❌ Exception during JWT validation from Kafka: {}", e.getMessage(), e);
                throw e; // rethrow so retries & DLT are triggered
            }
        } else {
            log.warn("⚠️ No JWT attached to this order event");
        }

        // ✅ Normal business logic goes here if no exception was thrown
        log.info("📦 Successfully processed order event: {}", event.getOrderId());
    }

    // ✅ Simple listener for Dead Letter Topic (orders-topic.DLT)
    @KafkaListener(
            topics = "#{__listener.topicName + '.DLT'}",
            groupId = "user-service-dlt",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenToDLT(OrderEvent event) {
        log.error("📥 Received message from DLT → {}", event);
    }
}
