package com.zhaoyijin.voiceshopping.controller;

import com.zhaoyijin.voiceshopping.dto.IntentResult;
import com.zhaoyijin.voiceshopping.service.IntentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class IntentDebugController {

    private final IntentService intentService;

    @PostMapping("/intent")
    public IntentResult classify(@RequestParam String sessionId,
                                 @RequestBody String utterance) {
        return intentService.classify(sessionId, utterance);
    }
}