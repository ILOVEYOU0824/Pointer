package com.safecontract.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToxicClauseDto {

    private final int id;
    private final String title;
    private final String originalText;
    private final String reason;
    private final String severity;
    private final Integer page;
    private final String suggestion;
}
