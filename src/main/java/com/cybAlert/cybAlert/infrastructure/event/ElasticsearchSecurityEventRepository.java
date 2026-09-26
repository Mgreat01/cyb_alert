package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public class ElasticsearchSecurityEventRepository implements SecurityEventRepository {

    private final SpringDataSecurityEventRepository repository;

    public ElasticsearchSecurityEventRepository(SpringDataSecurityEventRepository repository) {
        this.repository = repository;
    }

    public boolean existsById(String eventId) { return repository.existsById(eventId); }
    public Optional<SecurityEvent> findById(String eventId) { return repository.findById(eventId); }
    public SecurityEvent save(SecurityEvent event) { return repository.save(event); }

    public Page<SecurityEvent> search(String eventType, String sourceIp, String severity,
                                      Pageable pageable) {
        if (eventType != null) { return repository.findByEventType(eventType, pageable); }
        if (sourceIp != null) { return repository.findBySourceIp(sourceIp, pageable); }
        if (severity != null) { return repository.findBySeverity(severity, pageable); }
        return repository.findAll(pageable);
    }

    public long count() { return repository.count(); }
}
