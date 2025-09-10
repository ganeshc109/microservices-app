package com.example.demo.service;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import com.example.demo.aspect.TrackExecution;
import com.example.demo.dto.OrderEvent;

@Service
public class OrderProducerService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final String topicName;

    // Inject KafkaTemplate and NewTopic (from KafkaTopicConfig)
    public OrderProducerService(KafkaTemplate<String, OrderEvent> kafkaTemplate,
                                NewTopic ordersTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = ordersTopic.name(); // ✅ reuse topic from config
    }

   @TrackExecution
    public void sendOrderEvent(OrderEvent event) {
        kafkaTemplate.send(topicName, event); // ✅ no hardcoding
        System.out.println("📤 Sent Order event to [" + topicName + "]: " + event);
    }
}
