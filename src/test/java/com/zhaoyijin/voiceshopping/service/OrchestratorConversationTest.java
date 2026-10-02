package com.zhaoyijin.voiceshopping.service;

import com.zhaoyijin.voiceshopping.compliance.ComplianceChecker;
import com.zhaoyijin.voiceshopping.dto.*;
import com.zhaoyijin.voiceshopping.entity.*;
import com.zhaoyijin.voiceshopping.event.VoiceEventPublisher;
import com.zhaoyijin.voiceshopping.memory.*;
import com.zhaoyijin.voiceshopping.repository.ProductRepository;
import com.zhaoyijin.voiceshopping.voice.TtsService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrchestratorConversationTest {
    final IntentService intent = mock(IntentService.class);
    final ParallelRecommendService recommend = mock(ParallelRecommendService.class);
    final EmotionService emotion = mock(EmotionService.class);
    final SessionStateService states = mock(SessionStateService.class);
    final SessionService sessions = mock(SessionService.class);
    final ProductRepository products = mock(ProductRepository.class);
    final EmotionStreamingService streaming = mock(EmotionStreamingService.class);
    final TtsService tts = mock(TtsService.class);
    final AtomicReference<SessionStateEntity> saved = new AtomicReference<>();

    OrchestratorService orchestrator() {
        ComplianceChecker compliance = mock(ComplianceChecker.class);
        when(compliance.ensureCompliant(anyString(), anyLong(), any())).thenAnswer(i -> i.getArgument(2));
        doAnswer(i -> { saved.set(i.getArgument(0)); return null; }).when(states).save(any());
        when(tts.synthesize(any())).thenReturn(Flowable.empty());
        return new OrchestratorService(intent, null, recommend, emotion, null, states, sessions,
                mock(ShortTermMemory.class), mock(VoiceEventPublisher.class), compliance,
                new SimpleMeterRegistry(), products, new TurnSummarizer(), null, null, null,
                streaming, tts, mock(CachedTtsPhrases.class));
    }

    @Test
    void streamingRecommendationPersistsBudgetAndProductOrderForNextTurn() {
        SessionStateEntity state = new SessionStateEntity();
        state.setSessionId("conversation"); state.setPhase("INTENT"); state.setSlots(new HashMap<>());
        when(states.load("conversation")).thenReturn(state);
        when(intent.classify(anyString(), anyString())).thenReturn(
                new IntentResult(Intent.PRODUCT_RECOMMENDATION, Map.of("category", "运动鞋", "budget", 1000), .99));
        List<RecommendedItem> choices = List.of(item(7), item(10), item(6));
        when(recommend.recommend(anyString(), anyLong(), anyString(), anyMap()))
                .thenReturn(new RecommendResult(choices, "professional"));
        when(streaming.streamWrap(anyString(), anyString(), any())).thenReturn(Flux.just("这三款都在预算以内。"));

        List<StreamChunk> chunks = orchestrator().streamHandle("conversation", 1L, "预算1000运动鞋")
                .collectList().block();
        assertNotNull(saved.get(), "The recommendation must be saved before the next recording");
        assertEquals(Map.of("category", "运动鞋", "budget", 1000), saved.get().getSlots());
        assertEquals(List.of(7L, 10L, 6L), saved.get().getLastRecommendations());
        assertEquals("RECOMMEND", saved.get().getPhase());
        assertEquals(choices, chunks.get(0).products());
    }

    @Test
    void explainingTheseThreeUsesPreviousProductsInTheirDisplayedOrder() {
        SessionStateEntity state = new SessionStateEntity();
        state.setSessionId("conversation"); state.setPhase("RECOMMEND");
        state.setSlots(new HashMap<>(Map.of("category", "运动鞋", "budget", 1000)));
        state.setLastRecommendations(List.of(7L, 10L, 6L));
        when(states.load("conversation")).thenReturn(state);
        when(intent.classify(anyString(), anyString())).thenReturn(new IntentResult(Intent.PRODUCT_COMPARE, Map.of(), .99));
        when(products.findByIdIn(List.of(7L, 10L, 6L))).thenReturn(List.of(product(6), product(7), product(10)));
        when(recommend.recommend(anyString(), anyLong(), anyString(), anyMap()))
                .thenReturn(new RecommendResult(List.of(item(99)), "professional"));
        when(emotion.wrap(anyString(), anyString(), anyString(), any()))
                .thenAnswer(i -> new EmotionResult("介绍刚才的商品", ((RecommendResult)i.getArgument(3)).items()));

        EmotionResult reply = orchestrator().handle("conversation", 1L, "你给我详细介绍一下这三款鞋吧");
        assertEquals(List.of(7L, 10L, 6L), reply.displayBlocks().stream().map(RecommendedItem::productId).toList());
        assertEquals("鞋7", reply.displayBlocks().get(0).name());
    }

    static RecommendedItem item(long id) {
        return new RecommendedItem(id, "鞋" + id, BigDecimal.valueOf(599), "轻便", 1.0, Map.of());
    }
    static ProductEntity product(long id) {
        ProductEntity p = new ProductEntity(); p.setId(id); p.setName("鞋" + id);
        p.setPrice(BigDecimal.valueOf(599)); p.setAttributes(Map.of("weight", "light"));
        return p;
    }
}
