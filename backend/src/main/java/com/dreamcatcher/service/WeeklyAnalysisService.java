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
     * Processes a pending pattern analysis asynchronously.
     * Fetches dreams within the week's dates, calls the AI, and saves the output.
     */
    @Async
    @Transactional
    public void processReportAsync(Long reportId, java.time.LocalDate weekStart, java.time.LocalDate weekEnd) {
        log.info("Starting async pattern analysis for reportId={}", reportId);

        try {
            WeeklyReport report = weeklyReportRepository.findById(reportId).orElse(null);
            if (report == null || report.getStatus() != ReportStatus.PENDING) {
                log.warn("Report {} no longer exists or is not PENDING, aborting pattern analysis.", reportId);
                return;
            }

            Long userId = report.getUser().getId();
            
            // Fetch recent dreams with tags and sentiments within the date range
            List<com.dreamcatcher.entity.Dream> recentDreams = dreamRepository.findByUserIdWithTagsAndSentimentAndDateRange(
                    userId, weekStart, weekEnd
            );
            
            if (recentDreams.isEmpty()) {
                log.warn("No dreams found for reportId={}, marking as FAILED.", reportId);
                report.setStatus(ReportStatus.FAILED);
                weeklyReportRepository.save(report);
                return;
            }

            // Construct token-optimized JSON payload
            List<Map<String, Object>> minifiedDreams = recentDreams.stream()
                .filter(d -> d.getIsValid() != null && d.getIsValid())
                .filter(d -> d.getTags() != null && !d.getTags().isEmpty())
                .map(dream -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("day", dream.getDreamDate().getDayOfMonth());
                    
                    List<String> tags = dream.getTags().stream().map(DreamTag::getTag).toList();
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

            report.setInputTags(tagsJson);
            report.setReportContent(reportContentJson);
            report.setStatus(ReportStatus.COMPLETED);

            weeklyReportRepository.save(report);
            log.info("Subconscious pattern analysis completed successfully for reportId={}", reportId);

        } catch (Exception e) {
            log.error("Failed to generate weekly report for reportId={}: {}", reportId, e.getMessage());
            try {
                weeklyReportRepository.findById(reportId).ifPresent(r -> {
                    r.setStatus(ReportStatus.FAILED);
                    weeklyReportRepository.save(r);
                });
            } catch (Exception inner) {
                log.error("Failed to set status to FAILED for reportId={}", reportId);
            }
        }
    }

}
