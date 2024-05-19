package com.tiger.apigateway.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiger.apigateway.clients.IdentityClient;
import com.tiger.apigateway.exceptions.AuthLogicException;
import com.tiger.apigateway.exceptions.ErrorCode;
import com.tiger.apigateway.utils.ObjectMapperUtil;

import reactor.core.publisher.Mono;

@Configuration
public class AppConfig {

    //    @Value("${spring.cloud.gateway.routes[0].uri}")
    //    private String urlSsoService;

    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081/oauth")
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        resp -> Mono.just(AuthLogicException.builder()
                                .errorCode(ErrorCode.UNAUTHENTICATED)
                                .build()))
                .build();
    }

    @Bean
    IdentityClient identityClient(WebClient webClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(
                        WebClientAdapter.create(webClient))
                .build();

        return httpServiceProxyFactory.createClient(IdentityClient.class);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return ObjectMapperUtil.objectMapper();
    }
}
