package com.cybAlert.cybAlert.business.event;

import java.util.Optional;

public interface SecurityEventRepository {

    boolean existsById(String eventId);
    Optional<SecurityEvent> findById(String eventId);
    SecurityEvent save(SecurityEvent event);
}
