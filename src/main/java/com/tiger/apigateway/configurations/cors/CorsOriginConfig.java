package com.tiger.apigateway.configurations.cors;

import com.tiger.apigateway.constants.AppConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsOriginConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("*"));
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200")); // setAllowCredentials: true, support allow Credentials, Cookie, need to set allow http://localhost:4200

        // exposed header for read
        corsConfiguration.setExposedHeaders(List.of(AppConstants.APP_CONTENT_DISPOSITION, AppConstants.APP_REQUEST_ID));
        corsConfiguration.setAllowCredentials(true); // true: support allow Credentials, Cookie

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
