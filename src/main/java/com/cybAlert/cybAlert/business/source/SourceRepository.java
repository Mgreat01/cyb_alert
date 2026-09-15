package com.cybAlert.cybAlert.business.source;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SourceRepository {

    SourceEntity save(SourceEntity source);
    Optional<SourceEntity> findById(UUID id);
    Page<SourceEntity> findAll(Pageable pageable);
    boolean existsByHostname(String hostname);
    void delete(SourceEntity source);
}
