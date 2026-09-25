package com.cybAlert.cybAlert.business.risk;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RiskServiceTests {

    @Test
    void assignsExpectedWeightsToSecurityEvents() {
        RiskService risks = new RiskService(mock(StringRedisTemplate.class));

        assertThat(risks.weight("LOGIN_FAILED")).isEqualTo(5);
        assertThat(risks.weight("PORT_SCAN")).isEqualTo(20);
        assertThat(risks.weight("MALWARE_DETECTED")).isEqualTo(50);
        assertThat(risks.weight("PRIVILEGE_ESCALATION")).isEqualTo(80);
        assertThat(risks.weight("LOGIN_SUCCESS")).isZero();
    }
}
