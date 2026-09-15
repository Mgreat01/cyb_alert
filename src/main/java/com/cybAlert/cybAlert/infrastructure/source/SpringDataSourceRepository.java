package com.cybAlert.cybAlert.infrastructure.source;

import com.cybAlert.cybAlert.business.source.SourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataSourceRepository extends JpaRepository<SourceEntity, UUID> {

    boolean existsByHostnameIgnoreCase(String hostname);
}
