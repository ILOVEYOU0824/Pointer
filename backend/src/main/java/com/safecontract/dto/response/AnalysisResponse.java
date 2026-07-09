package com.safecontract.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AnalysisResponse {

    private final String fileName;
    private final int score;
    private final String scoreLabel;
    private final String summary;
    private final List<ToxicClauseDto> toxicClauses;
    private final List<String> analysisSteps;
}
