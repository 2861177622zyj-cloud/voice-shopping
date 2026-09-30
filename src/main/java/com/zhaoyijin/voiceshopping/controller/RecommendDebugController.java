package com.zhaoyijin.voiceshopping.controller;

import com.zhaoyijin.voiceshopping.dto.RecommendResult;
import com.zhaoyijin.voiceshopping.service.RecommendOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class RecommendDebugController {

    private final RecommendOrchestrator orchestrator;

    @PostMapping("/recommend")
    public RecommendResult recommend(@RequestParam String sessionId,
                                     @RequestParam Long userId,
                                     @RequestBody RecommendDebugReq req) {
        return orchestrator.recommend(sessionId, userId, req.utterance(), req.slots());
    }

    public record RecommendDebugReq(String utterance, Map<String, Object> slots) {}
}