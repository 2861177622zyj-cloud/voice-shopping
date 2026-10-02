package com.zhaoyijin.voiceshopping.service;

import com.zhaoyijin.voiceshopping.entity.SessionStateEntity;
import com.zhaoyijin.voiceshopping.repository.SessionStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionStateServiceTest {
    @Test
    void newSessionHasWritableEmptySlots() {
        SessionStateRepository repo = mock(SessionStateRepository.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(repo.findById("fresh")).thenReturn(Optional.empty());
        SessionStateEntity state = new SessionStateService(repo, redis, new ObjectMapper()).load("fresh");
        assertNotNull(state.getSlots());
        assertTrue(state.getSlots().isEmpty());
        state.getSlots().put("budget", 1000);
        assertEquals(1000, state.getSlots().get("budget"));
    }

    @Test
    void legacyCachedNullSlotsAreRepairedWhenLoaded() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("vs:session:legacy")).thenReturn(
                "{\"sessionId\":\"legacy\",\"phase\":\"INTENT\",\"slots\":null}");
        SessionStateEntity state = new SessionStateService(
                mock(SessionStateRepository.class), redis, new ObjectMapper()).load("legacy");
        assertNotNull(state.getSlots());
        assertTrue(state.getSlots().isEmpty());
    }
}
