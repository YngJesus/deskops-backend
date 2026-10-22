package com.portfolio.support.ticket;

import com.portfolio.support.audit.TicketHistory;
import com.portfolio.support.audit.TicketHistoryRepository;
import com.portfolio.support.notification.NotificationService;
import com.portfolio.support.shared.api.PageResponse;
import com.portfolio.support.shared.exception.BusinessRuleException;
import com.portfolio.support.shared.exception.NotFoundException;
import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import com.portfolio.support.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository tickets;
    private final TicketCommentRepository comments;
    private final TicketHistoryRepository history;
    private final UserRepository users;
    private final TicketWorkflow workflow;
    private final NotificationService notifications;

    @Transactional
    public TicketDtos.TicketResponse create(TicketDtos.CreateTicketRequest request, User actor) {
        Ticket ticket = tickets.save(new Ticket(request.subject(), request.description(), request.category(),
                request.priority(), actor));
        history.save(new TicketHistory(ticket, actor, "TICKET_CREATED", null, ticket.getStatus().name()));
        return TicketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public TicketDtos.TicketResponse get(UUID id, User actor) {
        Ticket ticket = find(id);
        requireVisible(ticket, actor);
        return TicketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketDtos.TicketResponse> search(User actor, TicketStatus status,
                                                          TicketPriority priority, TicketCategory category,
                                                          Instant from, Instant to, String query, Pageable pageable) {
        Specification<Ticket> spec = TicketSpecifications.visibleTo(actor)
                .and(TicketSpecifications.hasStatus(status))
                .and(TicketSpecifications.hasPriority(priority))
                .and(TicketSpecifications.hasCategory(category))
                .and(TicketSpecifications.createdAfter(from))
                .and(TicketSpecifications.createdBefore(to))
                .and(TicketSpecifications.containsText(query));
        Page<TicketDtos.TicketResponse> result = tickets.findAll(spec, pageable).map(TicketMapper::toResponse);
        return PageResponse.from(result);
    }

    @Transactional
    public TicketDtos.TicketResponse changeStatus(UUID id, TicketStatus target, User actor) {
        Ticket ticket = find(id);
        requireVisible(ticket, actor);
        TicketStatus old = ticket.getStatus();
        workflow.validate(ticket, target, actor);
        ticket.setStatus(target);
        if (target == TicketStatus.RESOLVED) ticket.setResolvedAt(Instant.now());
        if (target == TicketStatus.CLOSED) ticket.setClosedAt(Instant.now());
        if (target == TicketStatus.IN_PROGRESS && old == TicketStatus.RESOLVED) ticket.setResolvedAt(null);
        history.save(new TicketHistory(ticket, actor, "STATUS_CHANGED", old.name(), target.name()));
        if (!ticket.getCreatedBy().getId().equals(actor.getId())) {
            notifications.queue(ticket.getCreatedBy(), ticket, "STATUS_CHANGED",
                    "Ticket status changed from " + old + " to " + target);
        }
        return TicketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketDtos.TicketResponse assign(UUID id, UUID assigneeId, User actor) {
        requireAdmin(actor);
        Ticket ticket = find(id);
        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BusinessRuleException("A terminal ticket cannot be assigned");
        }
        User assignee = users.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
        if (assignee.getRole() != Role.SUPPORT || !assignee.isEnabled()) {
            throw new BusinessRuleException("Assignee must be an enabled SUPPORT user");
        }
        String old = ticket.getAssignee() == null ? null : ticket.getAssignee().getEmail();
        ticket.setAssignee(assignee);
        history.save(new TicketHistory(ticket, actor, "ASSIGNEE_CHANGED", old, assignee.getEmail()));
        notifications.queue(assignee, ticket, "TICKET_ASSIGNED", "Ticket was assigned to you");
        return TicketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketDtos.TicketResponse changePriority(UUID id, TicketPriority priority, User actor) {
        requireAdminOrSupport(actor);
        Ticket ticket = find(id);
        requireVisible(ticket, actor);
        TicketPriority old = ticket.getPriority();
        ticket.setPriority(priority);
        history.save(new TicketHistory(ticket, actor, "PRIORITY_CHANGED", old.name(), priority.name()));
        return TicketMapper.toResponse(ticket);
    }

    @Transactional
    public TicketDtos.CommentResponse addComment(UUID id, TicketDtos.AddCommentRequest request, User actor) {
        Ticket ticket = find(id);
        requireVisible(ticket, actor);
        if (ticket.getStatus() == TicketStatus.CLOSED || ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BusinessRuleException("Comments cannot be added to a terminal ticket");
        }
        if (request.internal() && actor.getRole() == Role.USER) {
            throw new AccessDeniedException("Customers cannot add internal comments");
        }
        TicketComment comment = comments.save(new TicketComment(ticket, actor, request.body(), request.internal()));
        history.save(new TicketHistory(ticket, actor, "COMMENT_ADDED", null,
                request.internal() ? "internal" : "public"));
        User recipient = actor.getId().equals(ticket.getCreatedBy().getId())
                ? ticket.getAssignee() : ticket.getCreatedBy();
        if (!request.internal() && recipient != null) {
            notifications.queue(recipient, ticket, "COMMENT_ADDED", "A new comment was added");
        }
        return TicketMapper.toResponse(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketDtos.CommentResponse> comments(UUID id, User actor, Pageable pageable) {
        Ticket ticket = find(id);
        requireVisible(ticket, actor);
        Page<TicketComment> result = actor.getRole() == Role.USER
                ? comments.findByTicketIdAndInternalFalse(id, pageable)
                : comments.findByTicketId(id, pageable);
        return PageResponse.from(result.map(TicketMapper::toResponse));
    }

    @Transactional
    public void delete(UUID id, User actor) {
        requireAdmin(actor);
        Ticket ticket = find(id);
        if (ticket.getStatus() != TicketStatus.CANCELLED) {
            throw new BusinessRuleException("Only cancelled tickets can be deleted");
        }
        tickets.delete(ticket);
    }

    public Ticket find(UUID id) {
        return tickets.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found"));
    }

    public void requireVisible(Ticket ticket, User actor) {
        boolean allowed = switch (actor.getRole()) {
            case ADMIN -> true;
            case SUPPORT -> ticket.getAssignee() != null && ticket.getAssignee().getId().equals(actor.getId());
            case USER -> ticket.getCreatedBy().getId().equals(actor.getId());
        };
        if (!allowed) throw new AccessDeniedException("Ticket is not visible to this user");
    }

    private void requireAdmin(User actor) {
        if (actor.getRole() != Role.ADMIN) throw new AccessDeniedException("Admin role required");
    }

    private void requireAdminOrSupport(User actor) {
        if (actor.getRole() == Role.USER) throw new AccessDeniedException("Support role required");
    }
}
