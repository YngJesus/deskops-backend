package com.portfolio.support.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("app.security")
public record SecurityProperties(@NotBlank String jwtSecret, Duration accessTokenTtl) {
}
