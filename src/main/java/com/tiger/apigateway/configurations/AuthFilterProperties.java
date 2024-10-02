package com.tiger.apigateway.configurations;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security")
public class AuthFilterProperties {
    private String[] publicEndpoints;

    @PostConstruct
    private void setDefaults() {
        if (publicEndpoints == null) {
            publicEndpoints = new String[] {"/internal/**"}; // Default value
        }
    }
}
