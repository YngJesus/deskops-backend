package com.portfolio.support.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, UUID> {
}
