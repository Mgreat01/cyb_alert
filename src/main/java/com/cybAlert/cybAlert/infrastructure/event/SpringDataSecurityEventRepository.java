package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

interface SpringDataSecurityEventRepository extends ElasticsearchRepository<SecurityEvent, String> {

    Page<SecurityEvent> findByEventType(String eventType, Pageable pageable);
    Page<SecurityEvent> findBySourceIp(String sourceIp, Pageable pageable);
    Page<SecurityEvent> findBySeverity(String severity, Pageable pageable);
    Page<SecurityEvent> findByEventTypeAndSourceIp(String eventType, String sourceIp, Pageable pageable);
    Page<SecurityEvent> findByEventTypeAndSeverity(String eventType, String severity, Pageable pageable);
    Page<SecurityEvent> findBySourceIpAndSeverity(String sourceIp, String severity, Pageable pageable);
    Page<SecurityEvent> findByEventTypeAndSourceIpAndSeverity(String eventType, String sourceIp,
                                                              String severity, Pageable pageable);
}
