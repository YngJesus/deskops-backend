package com.portfolio.support.api;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TicketApiIntegrationTest extends AbstractApiIntegrationTest {
    private User customer;
    private User support;
    private User admin;
    private String customerToken;
    private String supportToken;
    private String adminToken;

    @BeforeEach
    void setUpActors() throws Exception {
        customer = createUser("customer@example.com", Role.USER);
        support = createUser("support@example.com", Role.SUPPORT);
        admin = createUser("admin@example.com", Role.ADMIN);
        createUser("stranger@example.com", Role.USER);
        customerToken = login(customer.getEmail());
        supportToken = login(support.getEmail());
        adminToken = login(admin.getEmail());
    }

    @Test
    void executesTicketLifecycleWithRoleBasedAccess() throws Exception {
        String ticketId = createTicket();

        String strangerToken = login("stranger@example.com");
        mockMvc.perform(get("/api/v1/tickets/{id}", ticketId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/tickets/{id}/assign", ticketId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":\"%s\"}".formatted(support.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee.id").value(support.getId().toString()));

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .header("Authorization", bearer(supportToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));

        mockMvc.perform(post("/api/v1/tickets/{id}/comments", ticketId)
                        .header("Authorization", bearer(supportToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"body\":\"Internal investigation\",\"internal\":true}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tickets/{id}/comments", ticketId)
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .header("Authorization", bearer(supportToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvedAt").exists());

        mockMvc.perform(patch("/api/v1/tickets/{id}/status", ticketId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.closedAt").exists());

        mockMvc.perform(get("/api/v1/admin/tickets/{id}/history", ticketId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(6));
    }

    @Test
    void filtersCurrentUsersTickets() throws Exception {
        createTicket();

        mockMvc.perform(get("/api/v1/tickets/my")
                        .param("status", "OPEN")
                        .param("priority", "HIGH")
                        .param("q", "payment")
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].subject").value("Payment problem"));

        mockMvc.perform(get("/api/v1/tickets/my")
                        .param("priority", "LOW")
                        .header("Authorization", bearer(customerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void validatesTicketCreation() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subject\":\"\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.subject").exists())
                .andExpect(jsonPath("$.fieldErrors.category").exists())
                .andExpect(jsonPath("$.fieldErrors.priority").exists());
    }

    private String createTicket() throws Exception {
        String response = mockMvc.perform(post("/api/v1/tickets")
                        .header("Authorization", bearer(customerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "subject":"Payment problem",
                                  "description":"Payment for the order was rejected",
                                  "category":"BILLING",
                                  "priority":"HIGH"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }
}
