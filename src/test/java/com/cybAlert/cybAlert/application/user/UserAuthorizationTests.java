package com.cybAlert.cybAlert.application.user;

import com.cybAlert.cybAlert.business.user.UserService;
import com.cybAlert.cybAlert.infrastructure.auth.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
class UserAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @Test
    @WithMockUser(roles = "VIEWER")
    void preventsAViewerFromListingUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void allowsAnAdministratorToListUsers() throws Exception {
        when(service.findAll(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void registrationIsPublicButDoesNotBypassValidation() throws Exception {
        mockMvc.perform(post("/api/users").contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void onlyAdministratorCanUpdateAndDeleteUsers() throws Exception {
        String id = java.util.UUID.randomUUID().toString();
        mockMvc.perform(patch("/api/users/{id}", id)
                .with(user("viewer").roles("VIEWER"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/users/{id}", id)
                .with(user("admin").roles("ADMIN"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(delete("/api/users/{id}", id)
                .with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/{id}", id)
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }
}
