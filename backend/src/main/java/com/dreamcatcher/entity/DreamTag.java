package com.dreamcatcher.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AI-extracted keyword tag from a dream entry.
 * Each dream can have multiple tags (e.g., "ocean", "chase", "flying").
 * Tags are used in weekly analysis instead of raw dream text to optimize LLM token usage.
 */
@Entity
@Table(name = "dream_tags", indexes = {
        @Index(name = "idx_dream_tags_dream_id", columnList = "dream_id"),
        @Index(name = "idx_dream_tags_tag", columnList = "tag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DreamTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dream_id", nullable = false)
    private Dream dream;

    @Column(nullable = false, length = 100)
    private String tag;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
