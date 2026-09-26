package com.cybAlert.cybAlert.application.realtime;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/stream")
public class AlertStreamController {

    private final AlertUpdates updates;

    public AlertStreamController(AlertUpdates updates) { this.updates = updates; }

    @GetMapping(path = "/alerts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter alerts() { return updates.subscribe(); }
}
