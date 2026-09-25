package com.cybAlert.cybAlert.infrastructure.detection;

import com.cybAlert.cybAlert.business.detection.DetectionObservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SpringDataDetectionObservationRepository
        extends JpaRepository<DetectionObservationEntity, String> {

    long countByEventTypeAndSourceIpAndEventTimeBetween(
            String eventType, String sourceIp, Instant start, Instant end);

    @Query("select count(distinct o.destinationIp) from DetectionObservationEntity o "
            + "where o.eventType = :eventType and o.sourceIp = :sourceIp "
            + "and o.eventTime between :start and :end")
    long countDistinctDestinations(@Param("eventType") String eventType,
                                   @Param("sourceIp") String sourceIp,
                                   @Param("start") Instant start, @Param("end") Instant end);
}
