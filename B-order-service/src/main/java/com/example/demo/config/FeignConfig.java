package com.example.demo.config;

import feign.RequestInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    private static final Logger log = LoggerFactory.getLogger(FeignConfig.class);

    private final LoadBalancerClient loadBalancer;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public FeignConfig(LoadBalancerClient loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Bean
    public RequestInterceptor profileRoutingInterceptor() {
        return template -> {
            // Forward JWT token
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    template.header("Authorization", authHeader);
                }
            }

            // Service to call
            String serviceId = template.feignTarget().name();
            log.debug("Feign call targeting service [{}] with active profile [{}]", serviceId, activeProfile);

            // Use LoadBalancerClient to choose instance
            ServiceInstance instance = loadBalancer.choose(serviceId);
            if (instance == null) {
                log.error("❌ No instance found for [{}] via LoadBalancer", serviceId);
                return;
            }

            log.info("Routing Feign request for [{}] → {}:{} (profile={})",
                    serviceId, instance.getHost(), instance.getPort(),
                    instance.getMetadata().get("profile"));

            // Feign will append path, so use only base URI
            template.target(instance.getUri().toString());
        };
    }
}
