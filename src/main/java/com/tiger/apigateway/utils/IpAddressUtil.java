package com.tiger.apigateway.utils;

import java.util.Optional;

import org.springframework.web.server.ServerWebExchange;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class IpAddressUtil {

    public static String getIpAddress(ServerWebExchange exchange) {
        // Extracting the client IP address from request
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(address -> address.getAddress().getHostAddress())
                .orElse("unknown");
    }
}
