package com.zhaoyijin.voiceshopping.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserProfileSnapshotTest {
    private final JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();

    @Test
    void cachedProfileRetainsAllFieldsAfterSerialization() {
        UserProfileSnapshot profile = new UserProfileSnapshot(
                1L, "女", 28, 165, 55, "中性", "300-500",
                Map.of("鞋", 0.8), Map.of("品牌A", 0.6),
                List.of(10L, 11L), List.of(12L),
                new BigDecimal("0.75"), new BigDecimal("399.00"));

        assertEquals(profile, serializer.deserialize(serializer.serialize(profile)));
    }

    @Test
    void cachedProfileSupportsMissingOptionalFields() {
        UserProfileSnapshot profile = new UserProfileSnapshot(
                1L, null, null, null, null, null, null,
                Map.of(), Map.of(), List.of(), List.of(), null, null);

        assertEquals(profile, serializer.deserialize(serializer.serialize(profile)));
    }
}
