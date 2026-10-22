package com.portfolio.support.ticket;

import com.portfolio.support.user.User;

final class TicketMapper {
    private TicketMapper() {}

    static TicketDtos.TicketResponse toResponse(Ticket ticket) {
        return new TicketDtos.TicketResponse(
                ticket.getId(), ticket.getSubject(), ticket.getDescription(), ticket.getCategory(),
                ticket.getPriority(), ticket.getStatus(), user(ticket.getCreatedBy()), user(ticket.getAssignee()),
                ticket.getCreatedAt(), ticket.getUpdatedAt(), ticket.getResolvedAt(), ticket.getClosedAt(),
                ticket.getVersion());
    }

    static TicketDtos.CommentResponse toResponse(TicketComment comment) {
        return new TicketDtos.CommentResponse(comment.getId(), comment.getTicket().getId(),
                user(comment.getAuthor()), comment.getBody(), comment.isInternal(), comment.getCreatedAt());
    }

    static TicketDtos.UserSummary user(User user) {
        return user == null ? null : new TicketDtos.UserSummary(
                user.getId(), user.getDisplayName(), user.getEmail(), user.getRole().name());
    }
}
