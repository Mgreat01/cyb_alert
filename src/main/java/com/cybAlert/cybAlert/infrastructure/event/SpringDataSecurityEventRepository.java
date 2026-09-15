package com.cybAlert.cybAlert.infrastructure.event;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

interface SpringDataSecurityEventRepository extends ElasticsearchRepository<SecurityEvent, String> {
}
