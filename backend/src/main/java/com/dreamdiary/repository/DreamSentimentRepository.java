package com.dreamdiary.repository;

import com.dreamdiary.entity.DreamSentiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for DreamSentiment entity operations.
 */
@Repository
public interface DreamSentimentRepository extends JpaRepository<DreamSentiment, Long> {

    Optional<DreamSentiment> findByDreamId(Long dreamId);

    boolean existsByDreamId(Long dreamId);

}
