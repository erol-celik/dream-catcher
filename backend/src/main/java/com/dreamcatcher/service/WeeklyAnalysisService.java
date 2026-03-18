package com.dreamcatcher.service;

import com.dreamcatcher.ai.AiClient;
import com.dreamcatcher.ai.WeeklyAnalysisResult;
import com.dreamcatcher.entity.DreamTag;
import com.dreamcatcher.entity.User;
import com.dreamcatcher.entity.WeeklyReport;
import com.dreamcatcher.enums.ReportStatus;
import com.dreamcatcher.repository.DreamRepository;
import com.dreamcatcher.repository.UserRepository;
import com.dreamcatcher.repository.WeeklyReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;


/**
 * Service responsible for generating the 7th-dream weekly psychological report.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyAnalysisService {

    private final AiClient aiClient;
    private final DreamRepository dreamRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generates a subconscious pattern analysis asynchronously.
     * Fetches the last 5 or 7 dreams depending on the hybrid cycle trigger.
     */
    @Async
    @Transactional
    public void generateWeeklyReportAsync(Long userId, long totalDreams) {
        log.info("Starting async pattern analysis for user={}, totalDreams={}", userId, totalDreams);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User {} no longer exists, aborting pattern analysis.", userId);
                return;
            }

            int limit = (totalDreams == 5) ? 5 : 7;
            
            // Fetch recent dreams with tags and sentiments
            List<com.dreamcatcher.entity.Dream> recentDreams = dreamRepository.findByUserIdWithTagsAndSentiment(
                    userId, org.springframework.data.domain.PageRequest.of(0, limit)
            );
            
            if (recentDreams.isEmpty()) {
                log.info("No dreams found for user={}, skipping pattern analysis.", userId);
                return;
            }

            // Reverse to chronological order for better AI trend analysis
            java.util.Collections.reverse(recentDreams);

            // Construct token-optimized JSON payload
            List<Map<String, Object>> minifiedDreams = recentDreams.stream().map(dream -> {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("day", dream.getDreamDate().getDayOfMonth());
                
                List<String> tags = dream.getTags() != null ? 
                        dream.getTags().stream().map(DreamTag::getTag).toList() : 
                        java.util.Collections.emptyList();
                map.put("tags", tags);
                
                String sentiment = (dream.getSentiment() != null && dream.getSentiment().getSentiment() != null) ? 
                        dream.getSentiment().getSentiment().name() : "NEUTRAL";
                map.put("sentiment", sentiment);
                return map;
            }).toList();

            String tagsJson = objectMapper.writeValueAsString(minifiedDreams);
            log.debug("Sending minified dreams to AI for user={}: {}", userId, tagsJson);
            
            WeeklyAnalysisResult result = aiClient.generateWeeklyReport(tagsJson);

            String reportContentJson = objectMapper.writeValueAsString(result);

            // Save the report using the existing WeeklyReport structure
            WeeklyReport report = WeeklyReport.builder()
                    .user(user)
                    .weekStart(recentDreams.get(0).getDreamDate()) // first dream date
                    .weekEnd(recentDreams.get(recentDreams.size() - 1).getDreamDate()) // last dream date
                    .inputTags(tagsJson)
                    .reportContent(reportContentJson)
                    .dreamCount(limit)
                    .status(ReportStatus.COMPLETED)
                    .build();

            weeklyReportRepository.save(report);
            log.info("Subconscious pattern analysis generated successfully for user={}", userId);

        } catch (Exception e) {
            log.error("Failed to generate weekly report for user={}: {}", userId, e.getMessage());
            // Handled via standard unhandled error metric monitoring in production
        }
    }

}
