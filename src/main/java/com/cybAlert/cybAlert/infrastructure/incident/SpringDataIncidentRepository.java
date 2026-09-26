package com.cybAlert.cybAlert.infrastructure.incident;

import com.cybAlert.cybAlert.business.incident.IncidentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.List;

public interface SpringDataIncidentRepository extends JpaRepository<IncidentEntity, UUID> {

    Page<IncidentEntity> findByStatus(IncidentEntity.Status status, Pageable pageable);
    long countByStatusNot(IncidentEntity.Status status);

    @Query(value = """
            SELECT * FROM incidents
            WHERE status NOT IN ('CLOSED', 'FALSE_POSITIVE')
            ORDER BY CASE priority
                WHEN 'CRITICAL' THEN 4 WHEN 'HIGH' THEN 3
                WHEN 'MEDIUM' THEN 2 ELSE 1 END DESC,
                created_at DESC
            LIMIT 10
            """, nativeQuery = true)
    List<IncidentEntity> findPriorityIncidents();
}
