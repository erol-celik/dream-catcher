package com.dreamcatcher.service;

import com.dreamcatcher.ai.AiClient;
import com.dreamcatcher.ai.WeeklyAnalysisResult;
import com.dreamcatcher.entity.DreamTag;
import com.dreamcatcher.entity.User;
import com.dreamcatcher.entity.WeeklyReport;
import com.dreamcatcher.enums.ReportStatus;
import com.dreamcatcher.repository.DreamTagRepository;
import com.dreamcatcher.repository.UserRepository;
import com.dreamcatcher.repository.WeeklyReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating the 7th-dream weekly psychological report.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyAnalysisService {

    private final AiClient aiClient;
    private final DreamTagRepository dreamTagRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generates a weekly report asynchronously.
     * Fetches up to 35 most recent tags (5 tags per dream * 7 dreams),
     * groups them by frequency, and sends them to the AI.
     */
    @Async
    @Transactional
    public void generateWeeklyReportAsync(Long userId) {
        log.info("Starting async weekly report generation for user={}", userId);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("User {} no longer exists, aborting weekly report.", userId);
                return;
            }

            // Fetch recent tags for the week (proxying recent dreams via a custom query)
            // Note: A custom query in DreamTagRepository might be needed to get the exact last 7 dreams' tags.
            // For now, we fetch the most recent 35 tags for the user.
            List<DreamTag> recentTags = dreamTagRepository.findTop35ByDream_UserIdOrderByCreatedAtDesc(userId);
            
            if (recentTags.isEmpty()) {
                log.info("No tags found for user={}, skipping weekly report.", userId);
                return;
            }

            // Group tags by their string value and count frequencies to optimize LLM tokens
            Map<String, Long> tagFrequencies = recentTags.stream()
                    .collect(Collectors.groupingBy(DreamTag::getTag, Collectors.counting()));

            String tagsJson = objectMapper.writeValueAsString(tagFrequencies);

            log.debug("Sending tags to AI for user={}: {}", userId, tagsJson);
            WeeklyAnalysisResult result = aiClient.generateWeeklyReport(tagsJson);

            // Create JSON for the combined report content
            String reportContentJson = objectMapper.writeValueAsString(result);

            // Save the report
            WeeklyReport report = WeeklyReport.builder()
                    .user(user)
                    .weekStart(LocalDate.now().minusDays(7))
                    .weekEnd(LocalDate.now())
                    .inputTags(tagsJson)
                    .reportContent(reportContentJson)
                    .dreamCount(7) // trigger count
                    .status(ReportStatus.COMPLETED)
                    .build();

            weeklyReportRepository.save(report);
            log.info("Weekly report generated successfully for user={}", userId);

        } catch (Exception e) {
            log.error("Failed to generate weekly report for user={}: {}", userId, e.getMessage());
            // Handled via standard unhandled error metric monitoring in production
        }
    }

}
