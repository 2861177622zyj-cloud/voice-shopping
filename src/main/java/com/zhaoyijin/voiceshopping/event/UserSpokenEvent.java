package com.zhaoyijin.voiceshopping.event;

public record UserSpokenEvent(String sessionId, Long userId, String utterance, long timestamp) {
}