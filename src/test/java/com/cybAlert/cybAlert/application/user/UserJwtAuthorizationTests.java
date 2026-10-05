package com.cybAlert.cybAlert.application.user;

import com.cybAlert.cybAlert.business.user.UserService;
import com.cybAlert.cybAlert.infrastructure.auth.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
class UserJwtAuthorizationTests {

    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;
    @MockitoBean UserService users;

    @Test
    void signedAdminCanListAndReachUserUpdateValidation() throws Exception {
        when(users.findAll(any())).thenReturn(Page.empty());
        String bearer = "Bearer " + token("ADMIN");

        mvc.perform(get("/api/users").header("Authorization", bearer))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/users/{id}", UUID.randomUUID())
                .header("Authorization", bearer)
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void signedViewerCannotManageUsers() throws Exception {
        String bearer = "Bearer " + token("VIEWER");

        mvc.perform(get("/api/users").header("Authorization", bearer))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/users/{id}", UUID.randomUUID())
                .header("Authorization", bearer)
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidSignatureCannotAccessAdminRouteButRegistrationRemainsPublic() throws Exception {
        String tampered = token("ADMIN") + "invalid";

        mvc.perform(get("/api/users").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/users").contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    private String token(String role) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cyberwatch")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject(UUID.randomUUID().toString())
                .claim("roles", role)
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
