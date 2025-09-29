package com.example.apigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

@Validated
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    @NotBlank
    private String secret;
    private String expectedIssuer;
    @PositiveOrZero
    private long accessClockSkewSeconds = 30;
    private String roleClaim = "roles";
}
