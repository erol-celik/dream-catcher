package com.dreamdiary.entity;

import com.dreamdiary.enums.Sentiment;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI sentiment analysis result for a single dream.
 * One-to-one relationship with Dream ensures exactly one sentiment per dream.
 * The raw_ai_response field stores the complete AI response for debugging and auditing.
 */
@Entity
@Table(name = "dream_sentiments", indexes = {
        @Index(name = "idx_dream_sentiments_dream_id", columnList = "dream_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DreamSentiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dream_id", nullable = false, unique = true)
    private Dream dream;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentiment sentiment;

    @Column(precision = 3, scale = 2)
    private BigDecimal confidence;

    @Column(name = "raw_ai_response", columnDefinition = "JSON")
    private String rawAiResponse;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
