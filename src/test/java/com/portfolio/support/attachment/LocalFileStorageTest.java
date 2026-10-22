package com.portfolio.support.attachment;

import com.portfolio.support.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageTest {
    @TempDir Path tempDirectory;

    @Test
    void storesAndLoadsAllowedFile() throws Exception {
        LocalFileStorage storage = new LocalFileStorage(new StorageProperties(tempDirectory));
        MultipartFile file = new MockMultipartFile(
                "file", "report.pdf", "application/pdf", "content".getBytes());

        FileStorage.StoredFile stored = storage.store(file);

        assertThat(stored.originalName()).isEqualTo("report.pdf");
        assertThat(stored.storageKey()).endsWith(".pdf");
        assertThat(storage.load(stored.storageKey()).getContentAsByteArray())
                .isEqualTo("content".getBytes());
    }

    @Test
    void rejectsEmptyAndUnsupportedFiles() {
        LocalFileStorage storage = new LocalFileStorage(new StorageProperties(tempDirectory));

        assertThatThrownBy(() -> storage.store(new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0])))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("empty");

        assertThatThrownBy(() -> storage.store(new MockMultipartFile(
                "file", "script.exe", "application/octet-stream", new byte[]{1})))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Unsupported");
    }
}
