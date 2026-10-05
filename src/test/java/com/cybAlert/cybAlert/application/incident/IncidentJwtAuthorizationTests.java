package com.cybAlert.cybAlert.application.incident;

import com.cybAlert.cybAlert.business.incident.IncidentService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
class IncidentJwtAuthorizationTests {

    @Autowired MockMvc mvc;
    @Autowired JwtEncoder encoder;
    @MockitoBean IncidentService incidents;

    @Test
    void signedViewerCanReadButCannotCreateIncident() throws Exception {
        when(incidents.findAll(any(), any())).thenReturn(Page.empty());
        String token = token("VIEWER", Instant.now().plusSeconds(300));

        mvc.perform(get("/api/incidents").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(post("/api/incidents").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void signedAnalystCanReachIncidentValidation() throws Exception {
        mvc.perform(post("/api/incidents")
                .header("Authorization", "Bearer " + token("SOC_ANALYST",
                        Instant.now().plusSeconds(300)))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void expiredSignedTokenIsRejected() throws Exception {
        mvc.perform(get("/api/incidents")
                .header("Authorization", "Bearer " + token("ADMIN",
                        Instant.now().minusSeconds(60))))
                .andExpect(status().isUnauthorized());
    }

    private String token(String role, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cyberwatch")
                .issuedAt(Instant.now().minusSeconds(120))
                .expiresAt(expiresAt)
                .subject(UUID.randomUUID().toString())
                .claim("roles", role)
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
