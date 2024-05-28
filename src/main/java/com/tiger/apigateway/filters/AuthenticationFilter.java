package com.tiger.apigateway.filters;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiger.apigateway.constants.AppConstants;
import com.tiger.apigateway.dtos.response.ApiResponse;
import com.tiger.apigateway.services.IdentityService;
import com.tiger.apigateway.utils.ObjectMapperUtil;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    IdentityService identityService;
    ObjectMapper objectMapper;

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @NonFinal
    private String[] publicEndpoints = {".*/auth/login", ".*/auth/sign-up"};

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter....");
        List<String> requestIds = exchange.getRequest().getHeaders().get(AppConstants.APP_REQUEST_ID);
        ServerWebExchange newExchange;
        if (CollectionUtils.isEmpty(requestIds)) {
            // add request id
            // Tạo HttpHeaders mới và copy các header cũ từ request
            HttpHeaders headers = new HttpHeaders();
            headers.addAll(exchange.getRequest().getHeaders());

            // Thêm hoặc thay đổi header
            String requestIdGateway = UUID.randomUUID().toString();
            headers.add(AppConstants.APP_REQUEST_ID, requestIdGateway);

            // push request id to log
            MDC.put(AppConstants.APP_REQUEST_ID, requestIdGateway);

            // Tạo request mới với các header mới
            ServerHttpRequest newRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public HttpHeaders getHeaders() {
                    return headers;
                }
            };

            // Tạo exchange mới với request đã thay đổi
            newExchange = exchange.mutate().request(newRequest).build();
        } else {
            newExchange = exchange;
        }

        if (isPublicEndpoint(exchange.getRequest())) {
            return chain.filter(newExchange);
        }

        // Get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (CollectionUtils.isEmpty(authHeader)) return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace(AppConstants.KEY_BEARER, "");
        log.info("Token: {}", token.length());

        return identityService
                .introspect(token)
                .flatMap(introspectResponse -> {
                    if (introspectResponse.getData().isValid()) return chain.filter(newExchange);
                    else return unauthenticated(exchange.getResponse());
                })
                .onErrorResume(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    return unauthenticated(exchange.getResponse());
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .build();

        String body = ObjectMapperUtil.castToString(apiResponse);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }
}
