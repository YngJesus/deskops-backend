package com.portfolio.support.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class TicketDtos {
    private TicketDtos() {}

    public record CreateTicketRequest(
            @NotBlank @Size(max = 160) String subject,
            @NotBlank @Size(max = 10_000) String description,
            @NotNull TicketCategory category,
            @NotNull TicketPriority priority
    ) {}

    public record ChangeStatusRequest(@NotNull TicketStatus status) {}
    public record AssignTicketRequest(@NotNull UUID assigneeId) {}
    public record ChangePriorityRequest(@NotNull TicketPriority priority) {}
    public record AddCommentRequest(
            @NotBlank @Size(max = 10_000) String body,
            boolean internal
    ) {}

    public record UserSummary(UUID id, String displayName, String email, String role) {}

    public record TicketResponse(
            UUID id,
            String subject,
            String description,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            UserSummary createdBy,
            UserSummary assignee,
            Instant createdAt,
            Instant updatedAt,
            Instant resolvedAt,
            Instant closedAt,
            long version
    ) {}

    public record CommentResponse(UUID id, UUID ticketId, UserSummary author, String body,
                                  boolean internal, Instant createdAt) {}
}
