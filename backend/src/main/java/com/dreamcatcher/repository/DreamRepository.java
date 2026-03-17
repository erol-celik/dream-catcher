package com.dreamcatcher.repository;

import com.dreamcatcher.entity.Dream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Dream entity operations.
 * Supports client_id based idempotent lookup, date range queries,
 * and valid dream counting for the "7th dream" trigger.
 */
@Repository
public interface DreamRepository extends JpaRepository<Dream, Long> {

    Optional<Dream> findByClientId(String clientId);

    List<Dream> findByUserIdOrderByDreamDateDesc(Long userId);

    List<Dream> findByUserIdAndDreamDateBetween(Long userId, LocalDate start, LocalDate end);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(d) FROM Dream d WHERE d.user.id = :userId AND d.isValid = true")
    long countValidDreamsByUserId(@Param("userId") Long userId);

    boolean existsByClientId(String clientId);

    /**
     * Fetches dreams with tags and sentiment eagerly loaded in a single query.
     * Prevents N+1 lazy loading when mapping to DreamResponse.
     */
    @Query("SELECT DISTINCT d FROM Dream d " +
           "LEFT JOIN FETCH d.tags " +
           "LEFT JOIN FETCH d.sentiment " +
           "WHERE d.user.id = :userId " +
           "ORDER BY d.dreamDate DESC")
    List<Dream> findByUserIdWithTagsAndSentiment(@Param("userId") Long userId);

    /**
     * Fetches a single dream with tags and sentiment eagerly loaded.
     */
    @Query("SELECT d FROM Dream d " +
           "LEFT JOIN FETCH d.tags " +
           "LEFT JOIN FETCH d.sentiment " +
           "WHERE d.id = :id")
    Optional<Dream> findByIdWithTagsAndSentiment(@Param("id") Long id);

}
