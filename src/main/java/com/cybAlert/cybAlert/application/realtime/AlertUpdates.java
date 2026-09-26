package com.cybAlert.cybAlert.application.realtime;

import com.cybAlert.cybAlert.business.detection.DetectionService.AlertCreated;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertUpdates extends TextWebSocketHandler {

    private final Set<WebSocketSession> sockets = ConcurrentHashMap.newKeySet();
    private final Set<SseEmitter> streams = ConcurrentHashMap.newKeySet();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        streams.add(emitter);
        emitter.onCompletion(() -> streams.remove(emitter));
        emitter.onTimeout(() -> streams.remove(emitter));
        emitter.onError(error -> streams.remove(emitter));
        return emitter;
    }

    @TransactionalEventListener
    public void alertCreated(AlertCreated event) {
        UUID alertId = event.alertId();
        String payload = "{\"alertId\":\"" + alertId + "\"}";
        for (WebSocketSession socket : sockets) {
            try {
                if (socket.isOpen()) { socket.sendMessage(new TextMessage(payload)); }
            } catch (IOException exception) {
                sockets.remove(socket);
            }
        }
        for (SseEmitter stream : streams) {
            try {
                stream.send(SseEmitter.event().name("alert-created").data(payload));
            } catch (IOException exception) {
                streams.remove(stream);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sockets.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sockets.remove(session);
    }
}
