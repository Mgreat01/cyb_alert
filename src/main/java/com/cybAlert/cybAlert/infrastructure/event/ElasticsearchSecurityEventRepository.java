package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import com.cybAlert.cybAlert.business.event.SecurityEventRepository;
import org.springframework.stereotype.Repository;
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
}
