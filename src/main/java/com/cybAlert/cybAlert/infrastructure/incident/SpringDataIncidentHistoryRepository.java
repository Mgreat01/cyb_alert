package com.cybAlert.cybAlert.infrastructure.incident;

import com.cybAlert.cybAlert.business.incident.IncidentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataIncidentHistoryRepository
        extends JpaRepository<IncidentHistoryEntity, UUID> {

    List<IncidentHistoryEntity> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
