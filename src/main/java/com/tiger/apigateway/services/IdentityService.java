package com.tiger.apigateway.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.tiger.apigateway.clients.IdentityClient;
import com.tiger.apigateway.dtos.request.IntrospectRequest;
import com.tiger.apigateway.dtos.response.ApiResponse;
import com.tiger.apigateway.dtos.response.IntrospectResponse;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {

    IdentityClient identityClient;

    public Mono<ApiResponse<IntrospectResponse>> introspect(String token) {
        try {
            return identityClient.introspect(
                    IntrospectRequest.builder().token(token).build());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
