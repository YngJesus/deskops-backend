package com.portfolio.support.api;

import com.jayway.jsonpath.JsonPath;
import com.portfolio.support.attachment.TicketAttachmentRepository;
import com.portfolio.support.audit.TicketHistoryRepository;
import com.portfolio.support.notification.NotificationRepository;
import com.portfolio.support.ticket.TicketCommentRepository;
import com.portfolio.support.ticket.TicketRepository;
import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import com.portfolio.support.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ActiveProfiles("test")
@SpringBootTest(properties = {"debug=false", "logging.level.root=WARN"})
@AutoConfigureMockMvc
abstract class AbstractApiIntegrationTest {
    protected static final String PASSWORD = "Password123!";

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UserRepository users;
    @Autowired protected PasswordEncoder passwordEncoder;
    @Autowired private NotificationRepository notifications;
    @Autowired private TicketHistoryRepository history;
    @Autowired private TicketAttachmentRepository attachments;
    @Autowired private TicketCommentRepository comments;
    @Autowired private TicketRepository tickets;

    @BeforeEach
    void cleanDatabase() {
        notifications.deleteAll();
        history.deleteAll();
        attachments.deleteAll();
        comments.deleteAll();
        tickets.deleteAll();
        users.deleteAll();
    }

    protected User createUser(String email, Role role) {
        return users.save(new User(email, passwordEncoder.encode(PASSWORD), email, role));
    }

    protected String login(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
