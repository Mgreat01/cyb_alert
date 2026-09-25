package com.cybAlert.cybAlert.business.risk;

import com.cybAlert.cybAlert.business.event.SecurityEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RiskService {

    private static final String SCRIPT = """
            if redis.call('SET', KEYS[2], '1', 'NX', 'EX', 2592000) == false then
                return tonumber(redis.call('GET', KEYS[1]) or '0')
            end
            local current = tonumber(redis.call('GET', KEYS[1]) or '0')
            local next = math.min(100, math.max(0, current + tonumber(ARGV[1])))
            redis.call('SET', KEYS[1], next)
            return next
            """;

    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> updateScript;

    public RiskService(StringRedisTemplate redis) {
        this.redis = redis;
        updateScript = new DefaultRedisScript<>(SCRIPT, Long.class);
    }

    public int apply(SecurityEvent event) {
        int weight = weight(event.getEventType());
        if (weight == 0) { return score(event.getSourceId()); }
        Long result = redis.execute(updateScript,
                List.of(scoreKey(event.getSourceId()), "risk:processed:" + event.getEventId()),
                String.valueOf(weight));
        if (result == null) { throw new IllegalStateException("Redis n'a pas renvoyé de score"); }
        return result.intValue();
    }

    public int score(UUID sourceId) {
        String value = redis.opsForValue().get(scoreKey(sourceId));
        return value == null ? 0 : Integer.parseInt(value);
    }

    private String scoreKey(UUID sourceId) { return "risk:sources:" + sourceId; }

    int weight(String eventType) {
        return switch (eventType) {
            case "LOGIN_FAILED" -> 5;
            case "PORT_SCAN" -> 20;
            case "MALWARE_DETECTED" -> 50;
            case "PRIVILEGE_ESCALATION" -> 80;
            default -> 0;
        };
    }
}
