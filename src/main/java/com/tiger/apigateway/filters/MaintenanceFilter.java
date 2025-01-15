package com.tiger.apigateway.filters;

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
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaintenanceFilter implements GlobalFilter, Ordered {

    final IdentityService identityService;

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter maintenance filter....");
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

        // allow api get maintenance
        if (isMaintenanceEndpoint(request)) {
            return chain.filter(newExchange);
        }

        List<String> clientSites = request.getHeaders().get(AppConstants.APP_CLIENT_SITE);
        if (CollectionUtils.isEmpty(clientSites)) {
            return chain.filter(newExchange);
        }

        String appCode = clientSites.get(0);
        // Get token from authorization header
        return identityService
                .getMaintenanceStatus(appCode)
                .flatMap(introspectResponse -> {
                    var data = introspectResponse.getData();
                    log.info("data: {}", data.getIsActive().toString());
                    // OFF maintenance
                    if (Boolean.FALSE.equals(introspectResponse.getData().getIsActive())) return chain.filter(newExchange);
                    // ON maintenance
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
        return -1;
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
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message("Hệ thống đang bảo trì. Vui lòng quay lại sau.")
                .build();

        String body = ObjectMapperUtil.castToString(apiResponse);
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private boolean isMaintenanceEndpoint(ServerHttpRequest request) {
        return request.getURI().getPath().matches(apiPrefix + "/internal/v1/maintenance");
    }
}
