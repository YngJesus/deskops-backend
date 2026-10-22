package com.portfolio.support.ticket;

import com.portfolio.support.user.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

final class TicketSpecifications {
    private TicketSpecifications() {}

    static Specification<Ticket> visibleTo(User user) {
        return switch (user.getRole()) {
            case ADMIN -> Specification.unrestricted();
            case SUPPORT -> (root, query, cb) -> cb.equal(root.get("assignee"), user);
            case USER -> (root, query, cb) -> cb.equal(root.get("createdBy"), user);
        };
    }

    static Specification<Ticket> hasStatus(TicketStatus status) {
        return status == null ? Specification.unrestricted()
                : (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    static Specification<Ticket> hasPriority(TicketPriority priority) {
        return priority == null ? Specification.unrestricted()
                : (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    static Specification<Ticket> hasCategory(TicketCategory category) {
        return category == null ? Specification.unrestricted()
                : (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    static Specification<Ticket> createdAfter(Instant from) {
        return from == null ? Specification.unrestricted()
                : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    static Specification<Ticket> createdBefore(Instant to) {
        return to == null ? Specification.unrestricted()
                : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    static Specification<Ticket> containsText(String queryText) {
        if (queryText == null || queryText.isBlank()) return Specification.unrestricted();
        String pattern = "%" + queryText.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("subject")), pattern),
                cb.like(cb.lower(root.get("description")), pattern));
    }
}
