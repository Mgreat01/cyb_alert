package com.cybAlert.cybAlert.business.event;

public interface SecurityEventRepository {

    boolean existsById(String eventId);
    SecurityEvent save(SecurityEvent event);
}
