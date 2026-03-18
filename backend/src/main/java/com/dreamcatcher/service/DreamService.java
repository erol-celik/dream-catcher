package com.dreamcatcher.service;

import com.dreamcatcher.dto.request.CreateDreamRequest;
import com.dreamcatcher.dto.response.DreamResponse;
import com.dreamcatcher.entity.Dream;
import com.dreamcatcher.entity.DreamTag;
import com.dreamcatcher.entity.User;
import com.dreamcatcher.exception.DreamValidationException;
import com.dreamcatcher.exception.DuplicateResourceException;
import com.dreamcatcher.exception.ResourceNotFoundException;
import com.dreamcatcher.repository.DreamRepository;
import com.dreamcatcher.repository.DreamSentimentRepository;
import com.dreamcatcher.repository.UserRepository;
import com.dreamcatcher.validation.DreamTextValidator;
import com.dreamcatcher.enums.Sentiment;
import com.dreamcatcher.entity.DreamSentiment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Service handling dream CRUD operations, heuristic validation,
 * and weekly analysis trigger detection (the "7th Dream" rule).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DreamService {



    private final DreamRepository dreamRepository;
    private final UserRepository userRepository;
    private final DreamSentimentRepository dreamSentimentRepository;
    private final DreamTextValidator textValidator;
    private final StreakService streakService;
    private final UserService userService;
    private final DreamAnalysisService dreamAnalysisService;

    /**
     * Creates a new dream entry after heuristic validation.
     * If this is the 7th valid dream, signals that weekly analysis should be triggered.
     *
     * @param userId  The owner's user ID.
     * @param request The dream creation payload.
     * @return DreamResponse with the saved dream data.
     */
    @Transactional
    public DreamResponse createDream(Long userId, CreateDreamRequest request) {
        log.info("Creating dream for user={}, clientId={}", userId, request.clientId());

        // Idempotency check: skip if already synced
        if (dreamRepository.existsByClientId(request.clientId())) {
            throw new DuplicateResourceException("Dream", "clientId", request.clientId());
        }

        // Heuristic validation before any LLM call
        DreamTextValidator.ValidationResult validation = textValidator.validate(request.content());
        if (!validation.valid()) {
            throw new DreamValidationException(validation.errorMessage(), validation.errorCode());
        }

        User user = userService.findUserById(userId);

        // Persist the dream
        Dream dream = Dream.builder()
                .clientId(request.clientId())
                .user(user)
                .content(request.content())
                .wordCount(validation.wordCount())
                .isValid(true)
                .dreamDate(request.dreamDate())
                .build();
        dream = dreamRepository.save(dream);

        // Save initial sentiment if provided by client
        if (request.sentiment() != null) {
            Sentiment sentimentEnum = Sentiment.fromString(request.sentiment());
            DreamSentiment initialSentiment = DreamSentiment.builder()
                    .dream(dream)
                    .sentiment(sentimentEnum)
                    .confidence(new java.math.BigDecimal("1.00")) // User provided
                    .build();
            dreamSentimentRepository.save(initialSentiment);
        }

        // Update streak
        streakService.recordActivity(userId);

        // 1. Trigger async tag extraction and sentiment analysis
        dreamAnalysisService.analyzeDreamAsync(dream.getId(), dream.getContent());

        log.info("Dream created: id={}, wordCount={}", dream.getId(), dream.getWordCount());
        return toResponse(dream);
    }

    /**
     * Creates a dream without validation, used during bulk sync.
     * Each sync item runs in its own transaction so failures
     * are isolated (one failed item does not roll back the batch).
     *
     * @param userId  The owner's user ID (re-loaded inside the new transaction).
     * @param request The dream creation payload.
     * @return The persisted Dream entity.
     */
    @Transactional
    public Dream createDreamForSync(Long userId, CreateDreamRequest request) {
        // Skip if already exists (idempotent)
        return dreamRepository.findByClientId(request.clientId())
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

                    String content = request.content();
                    int wordCount = content.trim().split("\\s+").length;

                    Dream dream = Dream.builder()
                            .clientId(request.clientId())
                            .user(user)
                            .content(content)
                            .wordCount(wordCount)
                            .isValid(wordCount >= 10)
                            .dreamDate(request.dreamDate())
                            .build();
                    dream = dreamRepository.save(dream);

                    if (request.sentiment() != null) {
                        Sentiment sentimentEnum = Sentiment.fromString(request.sentiment());
                        DreamSentiment initialSentiment = DreamSentiment.builder()
                                .dream(dream)
                                .sentiment(sentimentEnum)
                                .confidence(new java.math.BigDecimal("1.00"))
                                .build();
                        dreamSentimentRepository.save(initialSentiment);
                    }
                    return dream;
                });
    }

    /**
     * Retrieves all dreams for a user, ordered by date descending.
     * Uses JOIN FETCH to eagerly load tags and sentiment (prevents N+1).
     */
    @Transactional(readOnly = true)
    public List<DreamResponse> getDreamsByUser(Long userId) {
        return dreamRepository.findByUserIdWithTagsAndSentiment(userId, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Retrieves a single dream by its server-side ID.
     * Uses JOIN FETCH to eagerly load tags and sentiment.
     * Verifies that the dream belongs to the authenticated user.
     */
    @Transactional(readOnly = true)
    public DreamResponse getDreamById(Long userId, Long dreamId) {
        Dream dream = dreamRepository.findByIdWithTagsAndSentiment(dreamId)
                .orElseThrow(() -> new ResourceNotFoundException("Dream", "id", dreamId));
        if (!dream.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Dream", "id", dreamId);
        }
        return toResponse(dream);
    }

    /**
     * Converts a Dream entity to a DreamResponse DTO.
     */
    private DreamResponse toResponse(Dream dream) {
        List<String> tags = dream.getTags() != null
                ? dream.getTags().stream().map(DreamTag::getTag).toList()
                : Collections.emptyList();

        String sentiment = dream.getSentiment() != null
                ? dream.getSentiment().getSentiment().name()
                : null;

        return new DreamResponse(
                dream.getId(),
                dream.getClientId(),
                dream.getContent(),
                dream.getWordCount(),
                dream.getIsValid(),
                dream.getDreamDate(),
                tags,
                sentiment,
                dream.getCreatedAt()
        );
    }

}
