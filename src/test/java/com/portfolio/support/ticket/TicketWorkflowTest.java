package com.portfolio.support.ticket;

import com.portfolio.support.shared.exception.BusinessRuleException;
import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketWorkflowTest {
    private final TicketWorkflow workflow = new TicketWorkflow();
    private final User customer = new User("user@test.dev", "hash", "User", Role.USER);
    private final User support = new User("support@test.dev", "hash", "Support", Role.SUPPORT);

    @Test
    void customerMayCancelOpenTicket() {
        Ticket ticket = new Ticket("Subject", "Description", TicketCategory.OTHER, TicketPriority.MEDIUM, customer);
        workflow.validate(ticket, TicketStatus.CANCELLED, customer);
    }

    @Test
    void customerCannotStartWork() {
        Ticket ticket = new Ticket("Subject", "Description", TicketCategory.OTHER, TicketPriority.MEDIUM, customer);
        ticket.setAssignee(support);
        assertThatThrownBy(() -> workflow.validate(ticket, TicketStatus.IN_PROGRESS, customer))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Customers may only cancel");
    }

    @Test
    void ticketMustBeAssignedBeforeWorkStarts() {
        Ticket ticket = new Ticket("Subject", "Description", TicketCategory.OTHER, TicketPriority.MEDIUM, customer);
        assertThatThrownBy(() -> workflow.validate(ticket, TicketStatus.IN_PROGRESS, support))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Assign a support employee");
    }

    @Test
    void terminalTicketCannotBeReopened() {
        Ticket ticket = new Ticket("Subject", "Description", TicketCategory.OTHER, TicketPriority.MEDIUM, customer);
        ticket.setStatus(TicketStatus.CLOSED);
        assertThatThrownBy(() -> workflow.validate(ticket, TicketStatus.IN_PROGRESS, support))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not allowed");
    }
}
