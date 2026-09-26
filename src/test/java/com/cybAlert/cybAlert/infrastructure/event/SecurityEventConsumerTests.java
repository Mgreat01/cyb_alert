package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.detection.DetectionService;
import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventRepository;
import com.cybAlert.cybAlert.business.risk.RiskService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityEventConsumerTests {

    @Test
    void invokesDetectionAndRiskForIndexedEvent() {
        SecurityEventRepository events = mock(SecurityEventRepository.class);
        DetectionService detection = mock(DetectionService.class);
        RiskService risk = mock(RiskService.class);
        SecurityEvent event = mock(SecurityEvent.class);
        when(events.findById("evt-1")).thenReturn(Optional.of(event));

        new SecurityEventConsumer(events, detection, risk).consume("evt-1");

        verify(detection).process(event);
        verify(risk).apply(event);
    }

    @Test
    void failsForMissingIndexDocumentSoKafkaCanRetry() {
        SecurityEventRepository events = mock(SecurityEventRepository.class);
        when(events.findById("evt-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new SecurityEventConsumer(events,
                mock(DetectionService.class), mock(RiskService.class)).consume("evt-missing"))
                .isInstanceOf(IllegalStateException.class);
    }
}
