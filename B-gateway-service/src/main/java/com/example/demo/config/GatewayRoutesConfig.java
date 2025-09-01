package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Configuration
public class GatewayRoutesConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayRoutesConfig.class);

    @Bean
    public RouteLocator dynamicRoutes(RouteLocatorBuilder builder, DiscoveryClient discoveryClient) {
        return builder.routes()

                // User Service Route
                .route("user-service-route", r -> r.path("/api/users/**")
                        .filters(f -> f.filter((exchange, chain) -> routeByProfile(exchange, chain, discoveryClient, "B-USER-SERVICE")))
                        .uri("lb://B-USER-SERVICE"))

                // Order Service Route
                .route("order-service-route", r -> r.path("/api/orders/**")
                        .filters(f -> f.filter((exchange, chain) -> routeByProfile(exchange, chain, discoveryClient, "B-ORDER-SERVICE")))
                        .uri("lb://B-ORDER-SERVICE"))

                .build();
    }

    /**
     * Routes request based on X-Profile header and service metadata.
     * Rejects if header missing or invalid.
     */
    private Mono<Void> routeByProfile(ServerWebExchange exchange,
                                      org.springframework.cloud.gateway.filter.GatewayFilterChain chain,
                                      DiscoveryClient discoveryClient,
                                      String serviceName) {

        // Get X-Profile header
        String profile = exchange.getRequest().getHeaders().getFirst("X-Profile");

        if (profile == null || (!profile.equalsIgnoreCase("test") && !profile.equalsIgnoreCase("prod"))) {
            log.warn("Rejected request: missing or invalid X-Profile header [{}]", profile);
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        // Find instance matching profile
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        Optional<ServiceInstance> target = instances.stream()
                .filter(i -> profile.equalsIgnoreCase(i.getMetadata().get("profile")))
                .findFirst();

        if (target.isEmpty()) {
            log.error("No service instance found for {} with profile {}", serviceName, profile);
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
        }

        URI targetUri = URI.create(target.get().getUri().toString() + exchange.getRequest().getPath().toString());
        ServerHttpRequest request = exchange.getRequest().mutate().uri(targetUri).build();
        log.info("Routing {} request for profile {} to URI {}", serviceName, profile, targetUri);

        return chain.filter(exchange.mutate().request(request).build());
    }
}
