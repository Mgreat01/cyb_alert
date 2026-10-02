package com.cybAlert.cybAlert.application;

import com.cybAlert.cybAlert.application.alert.AlertController;
import com.cybAlert.cybAlert.application.audit.AuditController;
import com.cybAlert.cybAlert.application.detection.DetectionRuleController;
import com.cybAlert.cybAlert.application.event.SecurityEventController;
import com.cybAlert.cybAlert.application.incident.IncidentController;
import com.cybAlert.cybAlert.application.source.SourceController;
import com.cybAlert.cybAlert.business.event.SecurityEventService;
import com.cybAlert.cybAlert.business.alert.AlertService;
import com.cybAlert.cybAlert.business.detection.DetectionRuleService;
import com.cybAlert.cybAlert.business.incident.IncidentService;
import com.cybAlert.cybAlert.business.source.SourceService;
import com.cybAlert.cybAlert.business.source.SourceEntity;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import com.cybAlert.cybAlert.infrastructure.audit.SpringDataAuditLogRepository;
import com.cybAlert.cybAlert.infrastructure.auth.SecurityConfiguration;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionRuleRepository;
import com.cybAlert.cybAlert.infrastructure.event.ElasticsearchSecurityEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SourceController.class, SecurityEventController.class,
        DetectionRuleController.class, AlertController.class, IncidentController.class,
        AuditController.class})
@Import(SecurityConfiguration.class)
class RoleAuthorizationTests {

    @Autowired MockMvc mvc;
    @MockitoBean SourceService sources;
    @MockitoBean SecurityEventService events;
    @MockitoBean ElasticsearchSecurityEventRepository indexedEvents;
    @MockitoBean SpringDataDetectionRuleRepository rules;
    @MockitoBean DetectionRuleService ruleService;
    @MockitoBean SpringDataAlertRepository alerts;
    @MockitoBean AlertService alertService;
    @MockitoBean IncidentService incidents;
    @MockitoBean SpringDataAuditLogRepository audit;

    @Test
    void operatorCanSubmitButViewerCannot() throws Exception {
        mvc.perform(post("/api/events").with(user("operator").roles("OPERATOR"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/events").with(user("viewer").roles("VIEWER"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void operatorCanCreateSourceButCannotChangeDetectionRules() throws Exception {
        mvc.perform(post("/api/sources").with(user("operator").roles("OPERATOR"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/detection-rules").with(user("operator").roles("OPERATOR"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/detection-rules").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analystCanManageAlertsAndIncidentsButViewerCannot() throws Exception {
        String id = UUID.randomUUID().toString();
        mvc.perform(patch("/api/alerts/{id}", id).with(user("analyst").roles("SOC_ANALYST"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(patch("/api/alerts/{id}", id).with(user("viewer").roles("VIEWER"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/incidents").with(user("analyst").roles("SOC_ANALYST"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/incidents").with(user("viewer").roles("VIEWER"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void onlyAdminCanReadAudit() throws Exception {
        when(audit.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Page.empty());
        mvc.perform(get("/api/audit").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/audit").with(user("analyst").roles("SOC_ANALYST")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/audit"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sourceModificationAndDeletionAreReservedToAdmin() throws Exception {
        String id = UUID.randomUUID().toString();
        mvc.perform(put("/api/sources/{id}", id).with(user("operator").roles("OPERATOR"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/sources/{id}", id).with(user("admin").roles("ADMIN"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(delete("/api/sources/{id}", id).with(user("operator").roles("OPERATOR")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/sources/{id}", id).with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void operatorCanHeartbeatAndViewerCanOnlyReadSources() throws Exception {
        String id = UUID.randomUUID().toString();
        when(sources.heartbeat(UUID.fromString(id))).thenReturn(
                org.mockito.Mockito.mock(SourceEntity.class));
        when(sources.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Page.empty());
        mvc.perform(patch("/api/sources/{id}/heartbeat", id)
                .with(user("operator").roles("OPERATOR")))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/sources/{id}/heartbeat", id)
                .with(user("viewer").roles("VIEWER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/sources").with(user("viewer").roles("VIEWER")))
                .andExpect(status().isOk());
    }

    @Test
    void ruleUpdatesNeedAdminAndIncidentUpdatesNeedAnalyst() throws Exception {
        String id = UUID.randomUUID().toString();
        mvc.perform(put("/api/detection-rules/{id}", id)
                .with(user("analyst").roles("SOC_ANALYST"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/detection-rules/{id}", id)
                .with(user("admin").roles("ADMIN"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/api/incidents/{id}/status", id)
                .with(user("viewer").roles("VIEWER"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/incidents/{id}/status", id)
                .with(user("analyst").roles("SOC_ANALYST"))
                .contentType("application/json").content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
