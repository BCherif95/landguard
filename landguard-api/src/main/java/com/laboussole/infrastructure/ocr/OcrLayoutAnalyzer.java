package com.laboussole.infrastructure.ocr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pure geometric heuristics over OCR word boxes to flag documents worth a
 * closer human look: inconsistent typefaces (fraudulent edits often mix
 * fonts) and irregular baseline alignment (stamps or mentions added over the
 * original print). No Tesseract dependency, fully unit-testable.
 *
 * <p>Anomaly strings are user-facing French. Heuristics are deliberately
 * conservative — they signal, they never judge (final review is human,
 * PRD Feature 02.2).
 */
final class OcrLayoutAnalyzer {

    /** One recognised word: text, engine confidence [0..100], bounding box. */
    record OcrWord(String text, float confidence, int x, int y, int width, int height) {

        double centerY() {
            return y + height / 2.0;
        }
    }

    private static final int MIN_WORDS_FOR_ANALYSIS = 10;
    private static final double LOW_QUALITY_MEAN_CONFIDENCE = 55;
    private static final double LOW_CONFIDENCE_WORD_THRESHOLD = 50;
    private static final double LOW_CONFIDENCE_RATIO_THRESHOLD = 0.30;
    private static final double HEIGHT_OUTLIER_RATIO_THRESHOLD = 0.25;
    private static final double MISALIGNED_LINE_RATIO_THRESHOLD = 0.30;
    /** Baseline jitter beyond ~a third of the glyph height is not print noise. */
    private static final double BASELINE_STDDEV_RATIO = 0.35;

    private OcrLayoutAnalyzer() {
    }

    static List<String> analyse(List<OcrWord> words) {
        List<String> anomalies = new ArrayList<>();
        if (words.size() < MIN_WORDS_FOR_ANALYSIS) {
            return anomalies;
        }

        double meanConfidence = words.stream().mapToDouble(OcrWord::confidence).average().orElse(0);
        if (meanConfidence < LOW_QUALITY_MEAN_CONFIDENCE) {
            anomalies.add("Qualité de numérisation insuffisante pour une lecture fiable.");
            // Too noisy for the finer heuristics below to mean anything.
            return anomalies;
        }

        if (hasInconsistentTypeface(words)) {
            anomalies.add("Police d'écriture non uniforme détectée dans le document.");
        }
        if (hasIrregularAlignment(words)) {
            anomalies.add("Alignement du texte irrégulier (tampon ou mention ajoutée suspecte).");
        }
        return anomalies;
    }

    /**
     * Mixed typefaces show up as a large share of low-confidence words in an
     * otherwise well-read document, or as strong glyph-height dispersion.
     */
    private static boolean hasInconsistentTypeface(List<OcrWord> words) {
        double lowConfidenceRatio = ratio(words,
                w -> w.confidence() < LOW_CONFIDENCE_WORD_THRESHOLD);
        if (lowConfidenceRatio > LOW_CONFIDENCE_RATIO_THRESHOLD) {
            return true;
        }
        double medianHeight = medianHeight(words);
        double heightOutlierRatio = ratio(words,
                w -> w.height() > medianHeight * 1.8 || w.height() < medianHeight * 0.5);
        return heightOutlierRatio > HEIGHT_OUTLIER_RATIO_THRESHOLD;
    }

    /** Lines whose words sit on visibly different baselines are suspicious. */
    private static boolean hasIrregularAlignment(List<OcrWord> words) {
        double medianHeight = medianHeight(words);
        var lines = groupIntoLines(words, medianHeight);

        long analysableLines = 0;
        long misalignedLines = 0;
        for (var line : lines) {
            if (line.size() < 3) {
                continue;
            }
            analysableLines++;
            double mean = line.stream().mapToDouble(OcrWord::centerY).average().orElse(0);
            double variance = line.stream()
                    .mapToDouble(w -> Math.pow(w.centerY() - mean, 2))
                    .average().orElse(0);
            if (Math.sqrt(variance) > medianHeight * BASELINE_STDDEV_RATIO) {
                misalignedLines++;
            }
        }
        return analysableLines > 0
                && (double) misalignedLines / analysableLines > MISALIGNED_LINE_RATIO_THRESHOLD;
    }

    private static List<List<OcrWord>> groupIntoLines(List<OcrWord> words, double medianHeight) {
        var sorted = words.stream().sorted(Comparator.comparingDouble(OcrWord::centerY)).toList();
        List<List<OcrWord>> lines = new ArrayList<>();
        List<OcrWord> current = new ArrayList<>();
        double currentLineY = Double.NaN;
        for (var word : sorted) {
            if (current.isEmpty() || Math.abs(word.centerY() - currentLineY) <= medianHeight) {
                current.add(word);
            } else {
                lines.add(current);
                current = new ArrayList<>();
                current.add(word);
            }
            currentLineY = current.stream().mapToDouble(OcrWord::centerY).average().orElse(word.centerY());
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        return lines;
    }

    private static double medianHeight(List<OcrWord> words) {
        var heights = words.stream().mapToInt(OcrWord::height).sorted().toArray();
        return heights.length % 2 == 1
                ? heights[heights.length / 2]
                : (heights[heights.length / 2 - 1] + heights[heights.length / 2]) / 2.0;
    }

    private static double ratio(List<OcrWord> words, java.util.function.Predicate<OcrWord> predicate) {
        return (double) words.stream().filter(predicate).count() / words.size();
    }
}
