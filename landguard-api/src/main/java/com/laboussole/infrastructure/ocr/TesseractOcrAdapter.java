package com.laboussole.infrastructure.ocr;

import com.laboussole.domain.model.ocr.OcrExtractionResult;
import com.laboussole.domain.model.parcel.DocumentType;
import com.laboussole.domain.port.out.DocumentOcrPort;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.util.PdfUtilities;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link DocumentOcrPort} backed by a local Tesseract engine (tess4j / JNA).
 *
 * <p><strong>Why Tesseract rather than a cloud OCR service:</strong>
 * <ol>
 *   <li><em>Cost</em> — free and open-source, no per-page billing, which
 *       matters at national registry volume;</li>
 *   <li><em>Data sovereignty</em> — land titles are sensitive state documents
 *       and must not transit a third-party cloud;</li>
 *   <li><em>Offline operation</em> — works without Internet connectivity,
 *       consistent with deployment constraints in Mali.</li>
 * </ol>
 * Swapping to a managed service later only requires a new implementation of
 * {@code DocumentOcrPort} — nothing else changes.
 *
 * <p>Requires the native {@code libtesseract} plus French trained data
 * ({@code fra.traineddata}) on the host; see {@code laboussole.ocr.*} in
 * {@code application.yml}. Any engine failure surfaces as
 * {@link OcrProcessingException} — the caller records a FAILED extraction.
 */
@Component
class TesseractOcrAdapter implements DocumentOcrPort {

    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F'};

    private final OcrProperties properties;

    TesseractOcrAdapter(OcrProperties properties) {
        this.properties = properties;
    }

    @Override
    public OcrExtractionResult extract(byte[] documentBytes, DocumentType type) {
        if (documentBytes == null || documentBytes.length == 0) {
            throw new OcrProcessingException("Empty document content");
        }
        var image = toImage(documentBytes);
        var engine = newEngine();
        try {
            String rawText = engine.doOCR(image);
            var words = engine.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD).stream()
                    .map(w -> new OcrLayoutAnalyzer.OcrWord(
                            w.getText(),
                            w.getConfidence(),
                            w.getBoundingBox().x,
                            w.getBoundingBox().y,
                            w.getBoundingBox().width,
                            w.getBoundingBox().height))
                    .toList();

            var fields = OcrTextFieldExtractor.extract(rawText);
            List<String> anomalies = new ArrayList<>(OcrLayoutAnalyzer.analyse(words));
            anomalies.addAll(OcrTextFieldExtractor.missingFieldAnomalies(fields, type));

            return new OcrExtractionResult(
                    fields.titleNumber(),
                    fields.ownerName(),
                    fields.surfaceAreaHectares(),
                    List.copyOf(anomalies));
        } catch (Exception | UnsatisfiedLinkError e) {
            throw new OcrProcessingException("Tesseract OCR failed: " + e.getMessage(), e);
        }
    }

    /** Tesseract instances are not thread-safe — one engine per extraction. */
    private ITesseract newEngine() {
        var tesseract = new Tesseract();
        if (properties.tessdataPath() != null && !properties.tessdataPath().isBlank()) {
            tesseract.setDatapath(properties.tessdataPath());
        }
        tesseract.setLanguage(properties.language());
        return tesseract;
    }

    /** First page only — registry documents carry their key fields up front. */
    private BufferedImage toImage(byte[] documentBytes) {
        try {
            if (isPdf(documentBytes)) {
                return firstPdfPageAsImage(documentBytes);
            }
            var image = ImageIO.read(new ByteArrayInputStream(documentBytes));
            if (image == null) {
                throw new OcrProcessingException("Unsupported image format");
            }
            return image;
        } catch (IOException e) {
            throw new OcrProcessingException("Cannot decode document content: " + e.getMessage(), e);
        }
    }

    private BufferedImage firstPdfPageAsImage(byte[] pdfBytes) throws IOException {
        File pdfFile = Files.createTempFile("ocr-", ".pdf").toFile();
        File[] pages = null;
        try {
            Files.write(pdfFile.toPath(), pdfBytes);
            pages = PdfUtilities.convertPdf2Png(pdfFile);
            if (pages == null || pages.length == 0) {
                throw new OcrProcessingException("PDF produced no pages to analyse");
            }
            var image = ImageIO.read(pages[0]);
            if (image == null) {
                throw new OcrProcessingException("Cannot decode rendered PDF page");
            }
            return image;
        } finally {
            if (pages != null) {
                for (File page : pages) {
                    Files.deleteIfExists(page.toPath());
                }
            }
            Files.deleteIfExists(pdfFile.toPath());
        }
    }

    private static boolean isPdf(byte[] bytes) {
        if (bytes.length < PDF_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }
}
