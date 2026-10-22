package com.portfolio.support.api;

import com.portfolio.support.user.Role;
import com.portfolio.support.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminApiIntegrationTest extends AbstractApiIntegrationTest {
    @Test
    void adminChangesRoleAndCannotDisableOwnAccount() throws Exception {
        User admin = createUser("admin@example.com", Role.ADMIN);
        User candidate = createUser("candidate@example.com", Role.USER);
        String token = login(admin.getEmail());

        mockMvc.perform(patch("/api/v1/admin/users/{id}/role", candidate.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"SUPPORT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SUPPORT"));

        mockMvc.perform(patch("/api/v1/admin/users/{id}/enabled", admin.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void regularUserCannotAccessAdminApi() throws Exception {
        User user = createUser("user@example.com", Role.USER);

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", bearer(login(user.getEmail()))))
                .andExpect(status().isForbidden());
    }
}
