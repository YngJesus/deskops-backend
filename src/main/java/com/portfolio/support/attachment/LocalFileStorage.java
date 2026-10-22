package com.portfolio.support.attachment;

import com.portfolio.support.shared.exception.BusinessRuleException;
import com.portfolio.support.shared.exception.NotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "application/pdf", "text/plain", "text/csv");
    private final Path root;

    public LocalFileStorage(StorageProperties properties) {
        this.root = properties.location().toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot initialize attachment storage", ex);
        }
    }

    @Override
    public StoredFile store(MultipartFile file) {
        if (file.isEmpty()) throw new BusinessRuleException("Attachment cannot be empty");
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessRuleException("Unsupported attachment type: " + contentType);
        }
        String originalName = sanitize(file.getOriginalFilename());
        String extension = extension(originalName);
        String storageKey = UUID.randomUUID() + extension;
        Path target = safeResolve(storageKey);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(storageKey, originalName, contentType, file.getSize());
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store attachment", ex);
        }
    }

    @Override
    public Resource load(String storageKey) {
        Path path = safeResolve(storageKey);
        if (!Files.isRegularFile(path)) throw new NotFoundException("Attachment content not found");
        return new FileSystemResource(path);
    }

    private Path safeResolve(String key) {
        Path result = root.resolve(key).normalize();
        if (!result.startsWith(root)) throw new BusinessRuleException("Invalid storage key");
        return result;
    }

    private String sanitize(String name) {
        if (name == null || name.isBlank()) return "attachment";
        String clean = Path.of(name).getFileName().toString().replaceAll("[\\r\\n]", "_");
        return clean.length() > 255 ? clean.substring(clean.length() - 255) : clean;
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || name.length() - dot > 12) return "";
        return name.substring(dot).toLowerCase().replaceAll("[^a-z0-9.]", "");
    }
}
