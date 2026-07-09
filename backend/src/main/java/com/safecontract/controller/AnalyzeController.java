package com.safecontract.controller;

import com.safecontract.dto.response.AnalysisResponse;
import com.safecontract.service.ContractAnalyzer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalyzeController {

    private final ContractAnalyzer contractAnalyzer;

    public AnalyzeController(ContractAnalyzer contractAnalyzer) {
        this.contractAnalyzer = contractAnalyzer;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyze(@RequestPart("file") MultipartFile file) {
        return contractAnalyzer.analyze(file);
    }
}
