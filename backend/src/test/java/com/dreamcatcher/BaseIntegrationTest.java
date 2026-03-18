package com.dreamcatcher;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;

/**
 * Base Abstract Context for Integration Tests.
 * Relies on Testcontainers with MySQL to provide an isolated database environment.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("dreamcatcher_test")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        mysql.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Provide a dummy JWT Secret for testing
        registry.add("app.jwt.secret", () -> "integration-test-secret-key-must-be-long-enough-for-hs256-algorithms-to-pass");
        
        // Dynamically provide MySQL container properties to Spring Context
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", mysql::getDriverClassName);
    }
}
