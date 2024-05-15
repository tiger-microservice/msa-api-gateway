package com.tiger.apigateway.clients;

import com.tiger.apigateway.dtos.request.IntrospectRequest;
import com.tiger.apigateway.dtos.response.ApiResponse;
import com.tiger.apigateway.dtos.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface IdentityClient {
    @PostExchange(url = "/v1/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}