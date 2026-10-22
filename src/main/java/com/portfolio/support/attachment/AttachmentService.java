package com.portfolio.support.attachment;

import com.portfolio.support.audit.TicketHistory;
import com.portfolio.support.audit.TicketHistoryRepository;
import com.portfolio.support.shared.exception.NotFoundException;
import com.portfolio.support.ticket.Ticket;
import com.portfolio.support.ticket.TicketService;
import com.portfolio.support.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {
    private final TicketAttachmentRepository attachments;
    private final TicketService ticketService;
    private final TicketHistoryRepository history;
    private final FileStorage storage;

    @Transactional
    public AttachmentDtos.AttachmentResponse upload(UUID ticketId, MultipartFile file, User actor) {
        Ticket ticket = ticketService.find(ticketId);
        ticketService.requireVisible(ticket, actor);
        FileStorage.StoredFile stored = storage.store(file);
        TicketAttachment attachment = attachments.save(new TicketAttachment(ticket, actor,
                stored.originalName(), stored.storageKey(), stored.contentType(), stored.sizeBytes()));
        history.save(new TicketHistory(ticket, actor, "ATTACHMENT_ADDED", null, attachment.getOriginalName()));
        return toResponse(attachment);
    }

    @Transactional(readOnly = true)
    public DownloadedAttachment download(UUID id, User actor) {
        TicketAttachment attachment = attachments.findById(id)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        ticketService.requireVisible(attachment.getTicket(), actor);
        Resource resource = storage.load(attachment.getStorageKey());
        return new DownloadedAttachment(resource, attachment.getOriginalName(), attachment.getContentType());
    }

    private AttachmentDtos.AttachmentResponse toResponse(TicketAttachment item) {
        return new AttachmentDtos.AttachmentResponse(item.getId(), item.getTicket().getId(),
                item.getOriginalName(), item.getContentType(), item.getSizeBytes(), item.getUploadedBy().getId(),
                item.getCreatedAt());
    }

    public record DownloadedAttachment(Resource resource, String fileName, String contentType) {}
}
