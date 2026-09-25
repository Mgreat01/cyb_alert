package com.cybAlert.cybAlert.business.event;

public interface EventPublisher {

    void publish(SecurityEvent event);
}
