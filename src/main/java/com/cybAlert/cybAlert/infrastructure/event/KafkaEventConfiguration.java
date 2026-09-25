package com.cybAlert.cybAlert.infrastructure.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class KafkaEventConfiguration {

    @Bean
    NewTopic securityEventsTopic() {
        return new NewTopic("security.events", 3, (short) 1);
    }
}
