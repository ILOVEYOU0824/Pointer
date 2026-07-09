package com.safecontract.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cors cors = new Cors();
    private Ai ai = new Ai();
    private Pdf pdf = new Pdf();

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
    }

    @Getter
    @Setter
    public static class Ai {
        private String apiKey = "";
        private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
        private String model = "gemini-2.5-flash-lite";
        private String visionModel = "gemini-2.5-flash-lite";
        private String fallbackModels = "gemini-2.5-flash";
        private int maxRetries = 2;
        private long retryDelayMs = 10000;
    }

    @Getter
    @Setter
    public static class Pdf {
        private int minTextLength = 100;
        private int maxPagesForOcr = 10;
    }
}
