package com.portfolio.support.ticket;

import com.portfolio.support.shared.exception.BusinessRuleException;
import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class TicketWorkflow {
    private final Map<TicketStatus, Set<TicketStatus>> transitions = new EnumMap<>(TicketStatus.class);

    public TicketWorkflow() {
        transitions.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        transitions.put(TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.WAITING_FOR_CUSTOMER,
                TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        transitions.put(TicketStatus.WAITING_FOR_CUSTOMER, EnumSet.of(TicketStatus.IN_PROGRESS,
                TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        transitions.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED));
        transitions.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
        transitions.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
    }

    public void validate(Ticket ticket, TicketStatus target, User actor) {
        if (ticket.getStatus() == target) throw new BusinessRuleException("Ticket already has status " + target);
        if (!transitions.get(ticket.getStatus()).contains(target)) {
            throw new BusinessRuleException("Transition from " + ticket.getStatus() + " to " + target + " is not allowed");
        }
        if (actor.getRole() == Role.USER && target != TicketStatus.CANCELLED) {
            throw new BusinessRuleException("Customers may only cancel their tickets");
        }
        if (target == TicketStatus.IN_PROGRESS && ticket.getAssignee() == null) {
            throw new BusinessRuleException("Assign a support employee before starting work");
        }
    }
}
