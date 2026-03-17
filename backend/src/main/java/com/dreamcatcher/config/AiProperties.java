package com.dreamcatcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the AI Integration Module.
 * Binds to `app.ai.*` in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "app.ai")
@Data
public class AiProperties {

    /**
     * The AI provider to use (e.g., "openai", "gemini").
     */
    private String provider = "openai";

    /**
     * Application API Key for the chosen provider.
     */
    private String apiKey = "";

    /**
     * Model to use (e.g., "gpt-4o-mini", "gemini-1.5-flash").
     */
    private String model = "gpt-4o-mini";

    /**
     * Timeout for AI HTTP requests in seconds.
     */
    private int timeoutSeconds = 30;

}
