package com.safecontract.service;

import com.safecontract.dto.response.AnalysisResponse;
import com.safecontract.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ContractAnalyzer {

    private final PdfTextExtractor pdfTextExtractor;
    private final AiAnalysisService aiAnalysisService;

    public ContractAnalyzer(PdfTextExtractor pdfTextExtractor, AiAnalysisService aiAnalysisService) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.aiAnalysisService = aiAnalysisService;
    }

    public AnalysisResponse analyze(MultipartFile file) {
        validateFile(file);

        try {
            byte[] pdfBytes = file.getBytes();
            PdfTextExtractor.ExtractionResult extraction = pdfTextExtractor.extract(pdfBytes);

            String contractText = extraction.text();
            if (extraction.needsOcr()) {
                contractText = aiAnalysisService.performOcr(extraction.pageImagesBase64());
            }

            if (contractText == null || contractText.isBlank()) {
                throw new InvalidFileException("PDF에서 텍스트를 추출할 수 없습니다.");
            }

            return aiAnalysisService.analyze(file.getOriginalFilename(), contractText);
        } catch (InvalidFileException e) {
            throw e;
        } catch (com.safecontract.exception.AnalysisException e) {
            throw e;
        } catch (Exception e) {
            throw new com.safecontract.exception.AnalysisException("계약서 분석 중 오류가 발생했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("파일이 비어 있습니다.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException("PDF 파일만 업로드 가능합니다.");
        }

        String contentType = file.getContentType();
        if (contentType != null
                && !contentType.equals("application/pdf")
                && !contentType.equals("application/octet-stream")) {
            throw new InvalidFileException("PDF 파일만 업로드 가능합니다.");
        }
    }
}
