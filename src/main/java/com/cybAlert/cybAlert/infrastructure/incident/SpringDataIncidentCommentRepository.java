package com.cybAlert.cybAlert.infrastructure.incident;

import com.cybAlert.cybAlert.business.incident.IncidentCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataIncidentCommentRepository
        extends JpaRepository<IncidentCommentEntity, UUID> {

    List<IncidentCommentEntity> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
