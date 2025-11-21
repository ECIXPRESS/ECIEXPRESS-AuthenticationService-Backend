package edu.dosw.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishEvent(String eventType, Map<String, Object> eventData) {
        try {
            Map<String, Object> completeEvent = new HashMap<>();
            completeEvent.put("eventId", java.util.UUID.randomUUID().toString());
            completeEvent.put("eventType", eventType);
            completeEvent.put("timestamp", java.time.Instant.now().toString());
            completeEvent.put("version", "1.0");
            completeEvent.put("data", eventData);

            String eventJson = objectMapper.writeValueAsString(completeEvent);

            String topic = "events." + eventType.toLowerCase();

            redisTemplate.convertAndSend(topic, completeEvent);

            log.info("Evento publicado - Tipo: {}, Topic: {}", eventType, topic);
            log.debug("Datos del evento: {}", eventJson);

        } catch (Exception e) {
            log.error("Error publicando evento {}: {}", eventType, e.getMessage(), e);
        }
    }
    public void publishLoginSuccess(String email, String userId, String name, String ip) {
        Map<String, Object> data = new HashMap<>();
        data.put("email", email);
        data.put("userId", userId);
        data.put("name", name);
        data.put("ip", ip);
        data.put("userAgent", "Web");

        publishEvent("login.success", data);
    }
}