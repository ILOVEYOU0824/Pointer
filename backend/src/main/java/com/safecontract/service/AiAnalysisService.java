package com.safecontract.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecontract.config.AppProperties;
import com.safecontract.dto.response.AnalysisResponse;
import com.safecontract.dto.response.ToxicClauseDto;
import com.safecontract.exception.AnalysisException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiAnalysisService {

    private static final String SYSTEM_PROMPT = """
            당신은 한국어 계약서·이용약관 전문 법률 분석 AI입니다.
            사용자에게 불리한 독소조항(일방적 해지, 과도한 면책, 불공정 손해배상, 개인정보 무제한 활용 등)을 찾아 분석하세요.

            반드시 아래 JSON 형식만 반환하세요. 다른 텍스트는 포함하지 마세요.
            {
              "score": 0~100 정수 (100=매우 안전, 0=매우 위험),
              "scoreLabel": "안전|보통|주의|위험" 중 하나,
              "summary": "2~3문장 요약",
              "toxicClauses": [
                {
                  "title": "조항 제목",
                  "originalText": "원문 일부",
                  "reason": "왜 위험한지",
                  "severity": "low|medium|high",
                  "page": 페이지 번호 또는 null,
                  "suggestion": "개선 제안"
                }
              ]
            }

            독소조항이 없으면 toxicClauses는 빈 배열, score는 90 이상으로 설정하세요.
            """;

    private final AppProperties appProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiAnalysisService(AppProperties appProperties, ObjectMapper objectMapper) {
        this.appProperties = appProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(appProperties.getAi().getBaseUrl())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    private static final int MAX_CONTRACT_CHARS = 8000;

    public AnalysisResponse analyze(String fileName, String contractText) {
        validateApiKey();

        String trimmedText = truncateText(contractText);
        String userPrompt = "다음 계약서/이용약관 텍스트를 분석해주세요:\n\n" + trimmedText;
        String aiResponse = callWithFallbackModels(
                appProperties.getAi().getModel(),
                SYSTEM_PROMPT,
                userPrompt,
                true
        );
        return parseResponse(fileName, aiResponse);
    }

    public String performOcr(List<String> pageImagesBase64) {
        validateApiKey();

        if (pageImagesBase64.isEmpty()) {
            throw new AnalysisException("OCR을 수행할 PDF 페이지가 없습니다.");
        }

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", "아래 계약서/이용약관 이미지에서 모든 텍스트를 한국어 그대로 추출해주세요. 설명 없이 텍스트만 반환하세요."));

        for (String imageBase64 : pageImagesBase64) {
            Map<String, Object> inlineData = new HashMap<>();
            inlineData.put("mime_type", "image/png");
            inlineData.put("data", imageBase64);
            parts.add(Map.of("inline_data", inlineData));
        }

        return callWithFallbackModels(
                appProperties.getAi().getVisionModel(),
                null,
                parts,
                false
        );
    }

    private void validateApiKey() {
        if (appProperties.getAi().getApiKey() == null || appProperties.getAi().getApiKey().isBlank()) {
            throw new AnalysisException("Gemini API 키가 설정되지 않았습니다. GEMINI_API_KEY 환경 변수를 확인하세요.");
        }
    }

    private String callWithFallbackModels(String primaryModel, String systemPrompt, String userPrompt, boolean jsonResponse) {
        return callWithFallbackModels(primaryModel, systemPrompt, List.of(Map.of("text", userPrompt)), jsonResponse);
    }

    private String callWithFallbackModels(String primaryModel, String systemPrompt, List<Map<String, Object>> parts, boolean jsonResponse) {
        List<String> models = buildModelCandidates(primaryModel);
        AnalysisException lastException = null;

        for (int i = 0; i < models.size(); i++) {
            String model = models.get(i);
            try {
                return callGeminiWithRetry(model, systemPrompt, parts, jsonResponse);
            } catch (AnalysisException e) {
                lastException = e;
                if (!isRetryableError(e.getMessage())) {
                    throw e;
                }
                if (i < models.size() - 1) {
                    sleep(resolveRetryDelayMs(e.getMessage()));
                }
            }
        }

        throw lastException != null
                ? lastException
                : new AnalysisException("Gemini API 호출에 실패했습니다.");
    }

    private List<String> buildModelCandidates(String primaryModel) {
        Set<String> models = new LinkedHashSet<>();
        models.add(primaryModel);

        String fallbackModels = appProperties.getAi().getFallbackModels();
        if (fallbackModels != null && !fallbackModels.isBlank()) {
            Arrays.stream(fallbackModels.split(","))
                    .map(String::trim)
                    .filter(model -> !model.isEmpty())
                    .forEach(models::add);
        }

        return List.copyOf(models);
    }

    private String callGeminiWithRetry(String model, String systemPrompt, List<Map<String, Object>> parts, boolean jsonResponse) {
        int maxRetries = appProperties.getAi().getMaxRetries();
        AnalysisException lastException = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return callGemini(model, systemPrompt, parts, jsonResponse);
            } catch (AnalysisException e) {
                lastException = e;
                if (!isRetryableError(e.getMessage()) || attempt == maxRetries - 1) {
                    throw e;
                }
                sleep(resolveRetryDelayMs(e.getMessage()));
            }
        }

        throw lastException != null
                ? lastException
                : new AnalysisException("Gemini API 호출에 실패했습니다.");
    }

    private boolean isRetryableError(String message) {
        if (message == null) {
            return false;
        }

        String lower = message.toLowerCase();
        return lower.contains("high demand")
                || lower.contains("try again")
                || lower.contains("overloaded")
                || lower.contains("unavailable")
                || lower.contains("resource_exhausted")
                || lower.contains("429")
                || lower.contains("503")
                || lower.contains("rate limit")
                || lower.contains("quota exceeded");
    }

    private long resolveRetryDelayMs(String message) {
        if (message != null) {
            Matcher matcher = Pattern.compile("retry in (\\d+(?:\\.\\d+)?)s", Pattern.CASE_INSENSITIVE)
                    .matcher(message);
            if (matcher.find()) {
                double seconds = Double.parseDouble(matcher.group(1));
                return (long) (seconds * 1000) + 2000;
            }
        }
        return appProperties.getAi().getRetryDelayMs();
    }

    private String truncateText(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= MAX_CONTRACT_CHARS) {
            return text;
        }
        return text.substring(0, MAX_CONTRACT_CHARS) + "\n\n...(이하 생략)";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnalysisException("분석이 중단되었습니다.", e);
        }
    }

    private String callGemini(String model, String systemPrompt, List<Map<String, Object>> parts, boolean jsonResponse) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", parts)));

        if (systemPrompt != null) {
            requestBody.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
        }

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", jsonResponse ? 0.2 : 0.1);
        if (jsonResponse) {
            generationConfig.put("responseMimeType", "application/json");
        } else {
            generationConfig.put("maxOutputTokens", 8192);
        }
        requestBody.put("generationConfig", generationConfig);

        try {
            JsonNode response = restClient.post()
                    .uri("/models/{model}:generateContent?key={apiKey}", model, appProperties.getAi().getApiKey())
                    .body(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
                        String errorBody = new String(res.getBody().readAllBytes());
                        String message = extractGeminiErrorMessage(errorBody);
                        throw new AnalysisException(toUserFriendlyMessage(message));
                    })
                    .body(JsonNode.class);

            if (response == null || response.has("error")) {
                String message = extractGeminiErrorMessage(response != null ? response.toString() : "empty response");
                throw new AnalysisException(toUserFriendlyMessage(message));
            }

            return response.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        } catch (AnalysisException e) {
            throw e;
        } catch (Exception e) {
            throw new AnalysisException("Gemini API 호출에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private String toUserFriendlyMessage(String message) {
        if (message == null || message.isBlank()) {
            return "AI 분석에 실패했습니다.";
        }

        String lower = message.toLowerCase();
        if (lower.contains("high demand") || lower.contains("try again later")) {
            return "AI 서버가 일시적으로 혼잡합니다. 잠시 후 다시 시도해 주세요.";
        }
        if (lower.contains("quota exceeded") || lower.contains("rate limit")) {
            return "AI 사용 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.";
        }
        if (lower.contains("api key not valid")) {
            return "Gemini API 키가 유효하지 않습니다.";
        }

        return message;
    }

    private String extractGeminiErrorMessage(String body) {
        try {
            JsonNode errorNode = objectMapper.readTree(body).path("error");
            if (!errorNode.isMissingNode()) {
                return errorNode.path("message").asText(body);
            }
        } catch (Exception ignored) {
        }
        return body;
    }

    private AnalysisResponse parseResponse(String fileName, String aiJson) {
        try {
            JsonNode root = objectMapper.readTree(extractJson(aiJson));

            List<ToxicClauseDto> clauses = new ArrayList<>();
            JsonNode clausesNode = root.path("toxicClauses");
            if (clausesNode.isArray()) {
                int index = 1;
                for (JsonNode clause : clausesNode) {
                    clauses.add(ToxicClauseDto.builder()
                            .id(index++)
                            .title(clause.path("title").asText("제목 없음"))
                            .originalText(clause.path("originalText").asText(""))
                            .reason(clause.path("reason").asText(""))
                            .severity(clause.path("severity").asText("medium"))
                            .page(clause.path("page").isNull() ? null : clause.path("page").asInt())
                            .suggestion(clause.path("suggestion").asText(""))
                            .build());
                }
            }

            return AnalysisResponse.builder()
                    .fileName(fileName)
                    .score(root.path("score").asInt(50))
                    .scoreLabel(root.path("scoreLabel").asText("보통"))
                    .summary(root.path("summary").asText(""))
                    .toxicClauses(clauses)
                    .analysisSteps(List.of(
                            "계약서를 읽는 중...",
                            "위험 조항을 분석하는 중...",
                            "법률 기준과 비교하는 중...",
                            "위험도를 계산하는 중...",
                            "AI 요약을 생성하는 중..."
                    ))
                    .build();
        } catch (Exception e) {
            throw new AnalysisException("AI 응답을 파싱할 수 없습니다.", e);
        }
    }

    private String extractJson(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return trimmed.substring(start, end + 1);
            }
        }
        return trimmed;
    }
}
