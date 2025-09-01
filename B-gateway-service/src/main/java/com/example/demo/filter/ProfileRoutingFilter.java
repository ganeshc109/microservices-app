package com.example.demo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class ProfileRoutingFilter implements GatewayFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ProfileRoutingFilter.class);

    private final DiscoveryClient discoveryClient;

    public ProfileRoutingFilter(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
        log.info(">>> ProfileRoutingFilter initialized successfully");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info(">>> Incoming request path: {}", path);

        String service = path.contains("/users") ? "b-user-service" : "b-order-service";

        String profileHeader = exchange.getRequest().getHeaders().getFirst("X-Profile");
        final String profile = (profileHeader == null || profileHeader.isBlank()) ? "test" : profileHeader;

        log.info(">>> Determined target service: {}, profile: {}", service, profile);

        List<ServiceInstance> instances = discoveryClient.getInstances(service).stream()
                .filter(i -> profile.equalsIgnoreCase(i.getMetadata().get("profile")))
                .collect(Collectors.toList());

        if (instances.isEmpty()) {
            log.warn(">>> No {} instances found for profile: {}", service, profile);
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
        }

        log.info(">>> Forwarding to {} instance(s): {}", service,
                instances.stream().map(ServiceInstance::getUri).toList());

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
