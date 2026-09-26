package com.cybAlert.cybAlert.business.detection;

import com.cybAlert.cybAlert.business.alert.AlertEntity;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.infrastructure.alert.SpringDataAlertRepository;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionObservationRepository;
import com.cybAlert.cybAlert.infrastructure.detection.SpringDataDetectionRuleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DetectionServiceTests {

    private SpringDataDetectionObservationRepository observations;
    private SpringDataDetectionRuleRepository rules;
    private SpringDataAlertRepository alerts;
    private DetectionService service;

    @BeforeEach
    void setUp() {
        observations = mock(SpringDataDetectionObservationRepository.class);
        rules = mock(SpringDataDetectionRuleRepository.class);
        alerts = mock(SpringDataAlertRepository.class);
        service = new DetectionService(observations, rules, alerts,
                mock(ApplicationEventPublisher.class));
    }

    @Test
    void createsAnAlertAtTheBruteForceThreshold() {
        when(rules.findByEventTypeAndEnabledTrue("LOGIN_FAILED"))
                .thenReturn(List.of(new DetectionRuleEntity("BRUTE_FORCE", "LOGIN_FAILED",
                        5, 60, "HIGH", true)));
        when(observations.countByEventTypeAndSourceIpAndEventTimeBetween(
                any(), any(), any(), any())).thenReturn(5L);
        when(alerts.saveAndFlush(any(AlertEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.process(event("LOGIN_FAILED", "evt-5", "10.0.0.1"));

        verify(alerts).saveAndFlush(any(AlertEntity.class));
    }

    @Test
    void ignoresAnAlreadyProcessedEvent() {
        when(observations.existsById("evt-5")).thenReturn(true);

        service.process(event("LOGIN_FAILED", "evt-5", "10.0.0.1"));

        verify(observations, never()).saveAndFlush(any());
        verify(alerts, never()).saveAndFlush(any());
    }

    @Test
    void waitsForDistinctDestinationsBeforeReportingAScan() {
        when(rules.findByEventTypeAndEnabledTrue("PORT_SCAN"))
                .thenReturn(List.of(new DetectionRuleEntity("NETWORK_SCAN", "PORT_SCAN",
                        10, 60, "HIGH", true)));
        when(observations.countDistinctDestinations(any(), any(), any(), any()))
                .thenReturn(9L);

        service.process(event("PORT_SCAN", "evt-9", "10.0.0.1"));

        verify(alerts, never()).saveAndFlush(any());
    }

    @Test
    void createsAnAlertWhenScanReachesDistinctDestinationThreshold() {
        when(rules.findByEventTypeAndEnabledTrue("PORT_SCAN"))
                .thenReturn(List.of(new DetectionRuleEntity("NETWORK_SCAN", "PORT_SCAN",
                        10, 60, "HIGH", true)));
        when(observations.countDistinctDestinations(any(), any(), any(), any()))
                .thenReturn(10L);
        when(alerts.saveAndFlush(any(AlertEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.process(event("PORT_SCAN", "evt-10", "10.0.0.1"));

        verify(alerts).saveAndFlush(any(AlertEntity.class));
    }

    private SecurityEvent event(String type, String id, String sourceIp) {
        return new SecurityEvent(id, type, Instant.now(), UUID.randomUUID(), sourceIp,
                "10.0.0.2", null, null, "TCP", null, "HIGH", "Message", Map.of());
    }
}
