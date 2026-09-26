package com.cybAlert.cybAlert.application.realtime;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration(proxyBeanMethods = false)
@EnableWebSocket
public class RealtimeConfiguration implements WebSocketConfigurer {

    private final AlertUpdates updates;

    public RealtimeConfiguration(AlertUpdates updates) { this.updates = updates; }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(updates, "/ws/notifications");
    }
}
