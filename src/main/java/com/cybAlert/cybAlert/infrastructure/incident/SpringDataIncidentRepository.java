package com.cybAlert.cybAlert.infrastructure.incident;

import com.cybAlert.cybAlert.business.incident.IncidentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataIncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    Page<IncidentEntity> findByStatus(IncidentEntity.Status status, Pageable pageable);
    long countByStatusNot(IncidentEntity.Status status);
}
