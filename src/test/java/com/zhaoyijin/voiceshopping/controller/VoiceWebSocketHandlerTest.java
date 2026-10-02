package com.zhaoyijin.voiceshopping.controller;

import com.zhaoyijin.voiceshopping.agent.AgentFactory;
import com.zhaoyijin.voiceshopping.service.*;
import com.zhaoyijin.voiceshopping.voice.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.*;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VoiceWebSocketHandlerTest {
    @Test void successfulStreamSendsCompletionEvent() throws Exception { check(false, "done"); }
    @Test void failedStreamSendsVisibleErrorEvent() throws Exception { check(true, "error"); }

    void check(boolean fail, String expectedType) throws Exception {
        AsrService asr = mock(AsrService.class);
        AtomicReference<BiConsumer<String, Boolean>> callback = new AtomicReference<>();
        when(asr.recognize(any(), any())).thenAnswer(i -> {
            callback.set(i.getArgument(1)); return new CompletableFuture<String>();
        });
        OrchestratorService orchestrator = mock(OrchestratorService.class);
        when(orchestrator.streamHandle("test", 1L, "介绍这三款"))
                .thenReturn(fail ? Flux.error(new IllegalStateException("private provider details")) : Flux.empty());
        WebSocketSession socket = mock(WebSocketSession.class);
        when(socket.isOpen()).thenReturn(true);
        when(socket.getId()).thenReturn("socket");
        when(socket.getAttributes()).thenReturn(Map.of("sessionId", "test", "userId", 1L));
        List<String> frames = new ArrayList<>();
        doAnswer(i -> { if (i.getArgument(0) instanceof TextMessage m) frames.add(m.getPayload()); return null; })
                .when(socket).sendMessage(any());
        ObjectMapper mapper = new ObjectMapper();
        VoiceWebSocketHandler handler = new VoiceWebSocketHandler(asr, mock(TtsService.class), mapper,
                orchestrator, mock(LongTermMemoryWriter.class), mock(AgentFactory.class));
        handler.afterConnectionEstablished(socket);
        callback.get().accept("介绍这三款", true);
        List<String> eventTypes = frames.stream().map(s -> mapper.readTree(s).path("type").asString()).toList();
        assertEquals(List.of("asr", expectedType), eventTypes);
        if (fail) assertFalse(frames.get(frames.size() - 1).contains("private provider details"));
    }
}
