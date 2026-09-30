package com.zhaoyijin.voiceshopping.controller;

import com.zhaoyijin.voiceshopping.dto.EmotionResult;
import com.zhaoyijin.voiceshopping.dto.RecommendResult;
import com.zhaoyijin.voiceshopping.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class EmotionDebugController {

    private final EmotionService emotionService;

    @PostMapping("/emotion")
    public EmotionResult wrap(@RequestParam String sessionId,
                              @RequestBody EmotionDebugReq req) {
        String userNeeds = req.userNeeds() == null ? "" : req.userNeeds();
        return emotionService.wrap(sessionId, req.utterance(), userNeeds, req.rec());
    }

    public record EmotionDebugReq(String utterance, String userNeeds, RecommendResult rec) {}
}