package com.dreamcatcher.repository;

import com.dreamcatcher.entity.DreamTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DreamTag entity operations.
 * Provides aggregated tag queries for weekly analysis and user insights.
 */
@Repository
public interface DreamTagRepository extends JpaRepository<DreamTag, Long> {

    void deleteByDream(com.dreamcatcher.entity.Dream dream);

    List<DreamTag> findByDreamId(Long dreamId);

    @Query("SELECT dt FROM DreamTag dt WHERE dt.dream.user.id = :userId")
    List<DreamTag> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT dt.tag, COUNT(dt) FROM DreamTag dt " +
           "WHERE dt.dream.user.id = :userId " +
           "GROUP BY dt.tag ORDER BY COUNT(dt) DESC")
    List<Object[]> findMostFrequentTagsByUserId(@Param("userId") Long userId);

    @Query("SELECT dt FROM DreamTag dt " +
           "WHERE dt.dream.user.id = :userId " +
           "AND dt.dream.id IN :dreamIds")
    List<DreamTag> findByUserIdAndDreamIds(@Param("userId") Long userId,
                                           @Param("dreamIds") List<Long> dreamIds);

    List<DreamTag> findTop35ByDream_UserIdOrderByCreatedAtDesc(Long userId);

}
