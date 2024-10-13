package com.tiger.apigateway.configurations.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tiger.apigateway.utils.IpAddressUtil;

import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            // Extracting the client IP address from request
            String ip = IpAddressUtil.getIpAddress(exchange);
            // Use custom key resolver to resolve IP-based keys
            return Mono.just(ip);
        };
    }
}
