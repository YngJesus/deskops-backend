package com.portfolio.support.attachment;

import java.time.Instant;
import java.util.UUID;

public final class AttachmentDtos {
    private AttachmentDtos() {}

    public record AttachmentResponse(UUID id, UUID ticketId, String fileName, String contentType,
                                     long sizeBytes, UUID uploadedById, Instant createdAt) {}
}
