package com.portfolio.support.attachment;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties("app.storage")
public record StorageProperties(Path location) {
}
