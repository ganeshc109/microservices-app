package com.example.demo.controller;

import com.example.demo.dto.OrderEvent;
import com.example.demo.service.OrderProducerService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

// REST API controller to accept new order requests
@RestController
@RequestMapping("/api/orders/kafka")
@RequiredArgsConstructor
public class OrderProducerController {

    private final OrderProducerService producer;

    // Example: POST /orders
    @PostMapping
    public String placeOrder(@RequestBody OrderEvent orderEvent,
                             HttpServletRequest request) {

        // Extract JWT from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            orderEvent.setJwtToken(jwt); // attach to event
        }

        producer.sendOrderEvent(orderEvent);
        return "✅ Order placed and event sent with JWT!";
    }


    @PostMapping("/batch")
    public String placeOrders(@RequestBody List<OrderEvent> orderEvents,
                            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        for (OrderEvent orderEvent : orderEvents) {
            // attach JWT if present
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                orderEvent.setJwtToken(authHeader.substring(7));
            }
            producer.sendOrderEvent(orderEvent); // send each event
        }

        return "✅ Batch of " + orderEvents.size() + " orders sent!";
    }
}
