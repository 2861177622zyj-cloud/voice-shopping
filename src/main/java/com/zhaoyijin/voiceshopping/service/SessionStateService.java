package com.zhaoyijin.voiceshopping.service;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import com.zhaoyijin.voiceshopping.entity.SessionStateEntity;
import com.zhaoyijin.voiceshopping.repository.SessionStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionStateService {

    private final SessionStateRepository repo;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    private String key(String sessionId) {
        return "vs:session:" + sessionId;
    }

    @SneakyThrows
    public SessionStateEntity load(String sessionId) {
        String cached = redis.opsForValue().get(key(sessionId));
        if (cached != null) return ensureSlots(mapper.readValue(cached, SessionStateEntity.class));
        Optional<SessionStateEntity> fromDb = repo.findById(sessionId);
        if (fromDb.isPresent()) {
            SessionStateEntity state = ensureSlots(fromDb.get());
            syncToRedis(state);
            return state;
        }
        SessionStateEntity init = new SessionStateEntity();
        init.setSessionId(sessionId);
        init.setPhase("INTENT");
        return ensureSlots(init);
    }

    @Transactional
    @SneakyThrows
    public void save(SessionStateEntity state) {
        repo.save(ensureSlots(state));
        try {
            syncToRedis(state);       // 缓存：Redis 后写，挂了吞掉
        } catch (org.springframework.data.redis.RedisConnectionFailureException e) {
            log.warn("Redis 同步失败，下次 load 会从 PG 重建 sessionId={}", state.getSessionId(), e);
        }
    }

    private SessionStateEntity ensureSlots(SessionStateEntity state) {
        if (state.getSlots() == null) state.setSlots(new HashMap<>());
        return state;
    }

    @SneakyThrows
    private void syncToRedis(SessionStateEntity state) {
        redis.opsForValue().set(key(state.getSessionId()),
                mapper.writeValueAsString(state),
                Duration.ofMinutes(30));
    }
}