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

import com.tiger.apigateway.properties.AuthFilterProperties;
import com.tiger.apigateway.constants.AppConstants;
import com.tiger.apigateway.dtos.response.ApiResponse;
import com.tiger.apigateway.services.IdentityService;
import com.tiger.apigateway.utils.IpAddressUtil;
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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    final IdentityService identityService;
    final AuthFilterProperties authFilterProperties;

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter....");
        ServerHttpRequest request = exchange.getRequest();

        List<String> requestIds = request.getHeaders().get(AppConstants.APP_REQUEST_ID);
        ServerWebExchange newExchange;
        if (CollectionUtils.isEmpty(requestIds)) {
            // add request id
            ServerHttpRequest newRequest = addRequestIdIntoRequestHeader(exchange);

            // Tạo exchange mới với request đã thay đổi
            newExchange = exchange.mutate().request(newRequest).build();
        } else {
            newExchange = exchange;
        }

        if (isPublicEndpoint(request)) {
            return chain.filter(newExchange);
        }

        // Get token from authorization header
        List<String> authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (CollectionUtils.isEmpty(authHeader)) return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace(AppConstants.KEY_BEARER, "");
        String url = request.getPath().value();
        String method = request.getMethod().name();
        log.info("Token length {} url {} method {}", token.length(), url, method);

        return identityService
                .introspect(token, url, method)
                .flatMap(introspectResponse -> {
                    if (introspectResponse.getData().isValid()) return chain.filter(newExchange);
                    else return unauthenticated(exchange.getResponse());
                })
                .onErrorResume(throwable -> {
                    log.error(throwable.getMessage(), throwable);
                    return otherError(exchange.getResponse(), throwable);
                });
    }

    private static ServerHttpRequest addRequestIdIntoRequestHeader(ServerWebExchange exchange) {
        // Tạo HttpHeaders mới và copy các header cũ từ request
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(exchange.getRequest().getHeaders());

        // Thêm hoặc thay đổi header
        String requestIdGateway = getValueUUID();
        log.info("[requestIdGateway] {}", requestIdGateway);
        headers.add(AppConstants.APP_REQUEST_ID, requestIdGateway);

        // extract ipAddress
        String ipAddress = IpAddressUtil.getIpAddress(exchange);
        log.info("[ipAddress] {}", ipAddress);
        headers.add(AppConstants.APP_REQUEST_ADDRESS, ipAddress);

        // push request id to log
        MDC.put(AppConstants.APP_REQUEST_ID, requestIdGateway);

        // Tạo request mới với các header mới
        return new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public HttpHeaders getHeaders() {
                return headers;
            }
        };
    }

    private static String getValueUUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replaceAll("-", "");
    }

    @Override
    public int getOrder() {
        return 0;
    }

    Mono<Void> otherError(ServerHttpResponse response, Throwable throwable) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .build();

        String body = ObjectMapperUtil.castToString(apiResponse);
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
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
        return Arrays.stream(this.authFilterProperties.getPublicEndpoints())
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }
}
