package com.cybAlert.cybAlert.infrastructure.source;

import com.cybAlert.cybAlert.business.source.SourceEntity;
import com.cybAlert.cybAlert.business.source.SourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaSourceRepository implements SourceRepository {

    private final SpringDataSourceRepository repository;

    public JpaSourceRepository(SpringDataSourceRepository repository) {
        this.repository = repository;
    }

    public SourceEntity save(SourceEntity source) { return repository.save(source); }
    public Optional<SourceEntity> findById(UUID id) { return repository.findById(id); }
    public Page<SourceEntity> findAll(Pageable pageable) { return repository.findAll(pageable); }
    public boolean existsByHostname(String hostname) {
        return repository.existsByHostnameIgnoreCase(hostname);
    }
    public void delete(SourceEntity source) { repository.delete(source); }
}
