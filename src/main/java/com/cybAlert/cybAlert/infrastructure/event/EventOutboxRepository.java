package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.EventOutboxEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface EventOutboxRepository extends JpaRepository<EventOutboxEntity, String> {

    List<EventOutboxEntity> findByPublishedAtIsNullOrderByCreatedAtAsc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EventOutboxEntity> findLockedByEventId(String eventId);
}
