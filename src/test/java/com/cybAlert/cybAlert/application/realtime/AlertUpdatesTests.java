package com.cybAlert.cybAlert.application.realtime;

import com.cybAlert.cybAlert.business.detection.DetectionService.AlertCreated;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AlertUpdatesTests {

    @Test
    void sendsOnlyAlertIdentifierToConnectedWebSocket() throws Exception {
        AlertUpdates updates = new AlertUpdates();
        WebSocketSession socket = mock(WebSocketSession.class);
        when(socket.isOpen()).thenReturn(true);
        updates.afterConnectionEstablished(socket);
        UUID id = UUID.randomUUID();

        updates.alertCreated(new AlertCreated(id));

        ArgumentCaptor<TextMessage> message = ArgumentCaptor.forClass(TextMessage.class);
        verify(socket).isOpen();
        verify(socket).sendMessage(message.capture());
        assertThat(message.getValue().getPayload()).contains(id.toString());
        assertThat(message.getValue().getPayload()).doesNotContain("password", "token");
        updates.afterConnectionClosed(socket, CloseStatus.NORMAL);
        updates.alertCreated(new AlertCreated(UUID.randomUUID()));
        verifyNoMoreInteractions(socket);
    }
}
