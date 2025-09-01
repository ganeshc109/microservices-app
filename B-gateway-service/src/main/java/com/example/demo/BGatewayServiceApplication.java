package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BGatewayServiceApplication {

    public static void main(String[] args) {
        System.out.println(">>> Starting B-Gateway-Service application...");
        SpringApplication.run(BGatewayServiceApplication.class, args);
    }
}
