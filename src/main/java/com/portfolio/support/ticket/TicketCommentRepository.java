package com.portfolio.support.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {
    Page<TicketComment> findByTicketId(UUID ticketId, Pageable pageable);
    Page<TicketComment> findByTicketIdAndInternalFalse(UUID ticketId, Pageable pageable);
}
