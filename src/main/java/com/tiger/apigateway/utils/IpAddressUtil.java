package com.tiger.apigateway.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IpAddressUtil {

    public static String getIpAddress(ServerWebExchange exchange) {
        // Extracting the client IP address from request
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(address -> address.getAddress().getHostAddress())
                .orElse("unknown");
    }
}
