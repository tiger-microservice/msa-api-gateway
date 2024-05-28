package com.tiger.apigateway.configurations.ratelimit;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

// Indicates this bean as the primary one if multiple implementations are available
@Primary
// Marks this class as a Spring component for component scanning and auto-configuration
@Component
public class CustomProxyAddressResolver implements KeyResolver {
    // Resolve method to determine the client address for rate limiting
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // Create an XForwardedRemoteAddressResolver with max trusted index
        XForwardedRemoteAddressResolver resolver = XForwardedRemoteAddressResolver.maxTrustedIndex(1);

        // Resolve the client's address from the exchange using the XForwardedRemoteAddressResolver
        InetSocketAddress inetSocketAddress = resolver.resolve(exchange);

        // Extract and return the host address from the resolved InetSocketAddress
        return Mono.just(inetSocketAddress.getAddress().getHostAddress());
    }
}
