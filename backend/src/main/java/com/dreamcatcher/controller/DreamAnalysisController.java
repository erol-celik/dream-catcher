package com.dreamcatcher.controller;

import com.dreamcatcher.ai.DreamAnalysisResult;
import com.dreamcatcher.service.DreamAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dreams")
@RequiredArgsConstructor
@Slf4j
public class DreamAnalysisController {

    private final DreamAnalysisService dreamAnalysisService;

    @PostMapping("/{dreamId}/analyze")
    public ResponseEntity<DreamAnalysisResult> analyzeDream(
            @PathVariable Long dreamId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal Long userId) {

        String content = request.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Received analysis request for dreamId={} from user={}", dreamId, userId);

        try {
            DreamAnalysisResult result = dreamAnalysisService.analyzeDreamSync(userId, dreamId, content);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            log.warn("Unauthorized attempt to analyze dream by userId={}", userId);
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            log.warn("Dream not found: {}", dreamId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Analysis failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
