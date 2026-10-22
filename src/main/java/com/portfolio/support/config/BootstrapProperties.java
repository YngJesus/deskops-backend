package com.portfolio.support.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.bootstrap")
public record BootstrapProperties(String adminEmail, String adminPassword) {
}
