package com.portfolio.support.attachment;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    StoredFile store(MultipartFile file);
    Resource load(String storageKey);

    record StoredFile(String storageKey, String originalName, String contentType, long sizeBytes) {}
}
