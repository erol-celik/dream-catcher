package com.dreamdiary.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for JWT authentication.
 * Binds to `app.jwt.*` in application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {

    /**
     * Secret key for signing the JWT (HS256 requires at least 256 bits/32 chars).
     */
    private String secret = "default-development-secret-key-must-be-long-enough-for-hs256";

    /**
     * JWT expiration time in milliseconds.
     * Default: 30 days (2592000000 ms)
     */
    private long expirationMs = 2592000000L;

}
