package com.dreamcatcher.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dream entry entity. Each record represents a single dream logged by a user.
 * The client_id field is a UUID generated on the client side to ensure
 * idempotent sync operations (prevents duplicate entries on retry).
 */
@Entity
@Table(name = "dreams", indexes = {
        @Index(name = "idx_dreams_user_id_dream_date", columnList = "user_id, dream_date"),
        @Index(name = "idx_dreams_client_id", columnList = "client_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dream extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, unique = true, length = 36)
    private String clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "word_count", nullable = false)
    private Integer wordCount;

    @Column(name = "is_valid", nullable = false)
    @Builder.Default
    private Boolean isValid = true;

    @Column(name = "dream_date", nullable = false)
    private LocalDate dreamDate;

    // ---- Relationships ----

    @OneToMany(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DreamTag> tags = new ArrayList<>();

    @OneToOne(mappedBy = "dream", cascade = CascadeType.ALL, orphanRemoval = true)
    private DreamSentiment sentiment;

}
