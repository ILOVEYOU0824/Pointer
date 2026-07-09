package com.safecontract.service;

import com.safecontract.config.AppProperties;
import com.safecontract.exception.AnalysisException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class PdfTextExtractor {

    private final AppProperties appProperties;

    public PdfTextExtractor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public ExtractionResult extract(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).trim();

            if (text.length() >= appProperties.getPdf().getMinTextLength()) {
                return new ExtractionResult(text, false, List.of());
            }

            List<String> pageImages = renderPages(document);
            return new ExtractionResult(text, true, pageImages);
        } catch (IOException e) {
            throw new AnalysisException("PDF 파일을 읽을 수 없습니다.", e);
        }
    }

    private List<String> renderPages(PDDocument document) throws IOException {
        PDFRenderer renderer = new PDFRenderer(document);
        int pageCount = Math.min(document.getNumberOfPages(), appProperties.getPdf().getMaxPagesForOcr());
        List<String> images = new ArrayList<>();

        for (int i = 0; i < pageCount; i++) {
            BufferedImage image = renderer.renderImageWithDPI(i, 150, ImageType.RGB);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            images.add(Base64.getEncoder().encodeToString(outputStream.toByteArray()));
        }

        return images;
    }

    public record ExtractionResult(String text, boolean needsOcr, List<String> pageImagesBase64) {}
}
