package com.tiger.apigateway.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiger.apigateway.constants.AppConstants;
import com.tiger.apigateway.utils.ObjectMapperUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class AppConfig {

    // setting cors
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setAllowedOrigins(List.of("*"));

        // exposed header for read
        corsConfiguration.setExposedHeaders(List.of(AppConstants.APP_CONTENT_DISPOSITION, AppConstants.APP_REQUEST_ID));
        corsConfiguration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return ObjectMapperUtil.objectMapper();
    }
}
