package com.tiger.apigateway.configurations.clients;

import com.tiger.apigateway.clients.IdentityClient;
import com.tiger.apigateway.exceptions.AuthLogicException;
import com.tiger.apigateway.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
public class ClientConfigs {

    @Value("${app.auth-service.url}")
    private String urlAuthService;

    @Bean
    WebClient webClient() {
        System.out.println(urlAuthService);
        return WebClient.builder()
                .baseUrl(urlAuthService + "/oauth")
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
}
