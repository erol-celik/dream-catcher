package com.dreamcatcher.service;

import com.dreamcatcher.ai.AiClient;
import com.dreamcatcher.ai.DreamAnalysisResult;
import com.dreamcatcher.entity.Dream;
import com.dreamcatcher.entity.DreamSentiment;
import com.dreamcatcher.entity.DreamTag;
import com.dreamcatcher.enums.Sentiment;
import com.dreamcatcher.repository.DreamRepository;
import com.dreamcatcher.repository.DreamSentimentRepository;
import com.dreamcatcher.repository.DreamTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsible for asynchronous AI processing of new dreams.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DreamAnalysisService {

    private final AiClient aiClient;
    private final DreamRepository dreamRepository;
    private final DreamTagRepository dreamTagRepository;
    private final DreamSentimentRepository dreamSentimentRepository;

    /**
     * Triggers AI analysis asynchronously to extract tags and sentiment without blocking the client.
     */
    @Async
    @Transactional
    public void analyzeDreamAsync(Long dreamId, String content) {
        log.info("Starting async AI analysis for dreamId={}", dreamId);

        try {
            DreamAnalysisResult result = aiClient.analyzeDream(content);
            log.info("AI Analysis complete. Tags: {}, Sentiment: {}", result.tags(), result.sentiment());

            Dream dream = dreamRepository.findById(dreamId).orElse(null);
            if (dream == null) {
                log.warn("Dream {} no longer exists, aborting analysis save.", dreamId);
                return;
            }

            // Save Tags
            if (result.tags() != null) {
                List<DreamTag> tags = result.tags().stream().map(tag ->
                        DreamTag.builder()
                                .dream(dream)
                                .tag(tag.trim().toLowerCase())
                                .build()
                ).toList();
                dreamTagRepository.saveAll(tags);
            }

            // Save Sentiment
            if (result.sentiment() != null) {
                try {
                    Sentiment sentimentEnum = Sentiment.valueOf(result.sentiment().toUpperCase());
                    DreamSentiment sentiment = DreamSentiment.builder()
                            .dream(dream)
                            .sentiment(sentimentEnum)
                            .build();
                    dreamSentimentRepository.save(sentiment);
                } catch (IllegalArgumentException e) {
                    log.error("AI returned invalid sentiment category: {}", result.sentiment());
                }
            }

        } catch (Exception e) {
            log.error("Async AI analysis failed for dreamId={}: {}", dreamId, e.getMessage());
            // In a production app, we might save a failure status or schedule a retry
        }
    }

    /**
     * Triggers AI analysis synchronously designed to be called directly by a user request,
     * returning the result immediately to the client.
     */
    @Transactional
    public DreamAnalysisResult analyzeDreamSync(Long userId, Long dreamId, String content) {
        log.info("Starting sync AI analysis for userId={}, dreamId={}", userId, dreamId);

        Dream dream = dreamRepository.findById(dreamId)
                .orElseThrow(() -> new IllegalArgumentException("Dream not found"));
        
        // Ensure the dream belongs to the requesting user
        if (!dream.getUser().getId().equals(userId)) {
            throw new SecurityException("User does not own this dream");
        }

        DreamAnalysisResult result = aiClient.analyzeDream(content);
        log.info("AI Analysis complete. Tags: {}, Sentiment: {}", result.tags(), result.sentiment());

        // Save Tags
        if (result.tags() != null && !result.tags().isEmpty()) {
            // clear old tags to prevent duplicates if analyzed multiple times
            dreamTagRepository.deleteByDream(dream);
            
            List<DreamTag> tags = result.tags().stream().map(tag ->
                    DreamTag.builder()
                            .dream(dream)
                            .tag(tag.trim().toLowerCase())
                            .build()
            ).toList();
            dreamTagRepository.saveAll(tags);
        }

        // Save Sentiment
        if (result.sentiment() != null) {
            try {
                Sentiment sentimentEnum = Sentiment.valueOf(result.sentiment().toUpperCase());
                DreamSentiment sentiment = dreamSentimentRepository.findByDream(dream).orElseGet(DreamSentiment::new);
                sentiment.setDream(dream);
                sentiment.setSentiment(sentimentEnum);
                dreamSentimentRepository.save(sentiment);
            } catch (IllegalArgumentException e) {
                log.error("AI returned invalid sentiment category: {}", result.sentiment());
            }
        }

        return result;
    }

}
