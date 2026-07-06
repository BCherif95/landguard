package com.laboussole.infrastructure.ocr;

import com.laboussole.infrastructure.ocr.OcrLayoutAnalyzer.OcrWord;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OcrLayoutAnalyzerTest {

    @Test
    void cleanUniformDocumentRaisesNoAnomaly() {
        assertThat(OcrLayoutAnalyzer.analyse(uniformWords(30, 90f))).isEmpty();
    }

    @Test
    void tooFewWordsMeansNoAnalysis() {
        assertThat(OcrLayoutAnalyzer.analyse(uniformWords(5, 20f))).isEmpty();
    }

    @Test
    void globallyPoorScanIsFlaggedAsLowQuality() {
        var anomalies = OcrLayoutAnalyzer.analyse(uniformWords(30, 40f));

        assertThat(anomalies)
                .containsExactly("Qualité de numérisation insuffisante pour une lecture fiable.");
    }

    @Test
    void mixedConfidenceInGoodScanIsFlaggedAsInconsistentTypeface() {
        List<OcrWord> words = new ArrayList<>();
        // 60% crisp words, 40% poorly recognised ones — typical of a re-typed zone.
        words.addAll(uniformWords(18, 95f));
        for (int i = 0; i < 12; i++) {
            words.add(word(i * 60, 500, 50, 20, 30f));
        }

        var anomalies = OcrLayoutAnalyzer.analyse(words);

        assertThat(anomalies)
                .contains("Police d'écriture non uniforme détectée dans le document.");
    }

    @Test
    void wordsOffTheirLineBaselineAreFlaggedAsIrregularAlignment() {
        List<OcrWord> words = new ArrayList<>();
        // Three lines of four words each; every line has one word floating
        // far off the shared baseline (like a stamp overlapping the text).
        for (int line = 0; line < 3; line++) {
            int baseY = 100 + line * 100;
            for (int i = 0; i < 3; i++) {
                words.add(word(i * 80, baseY, 60, 20, 92f));
            }
            words.add(word(3 * 80, baseY + 18, 60, 20, 92f));
        }

        var anomalies = OcrLayoutAnalyzer.analyse(words);

        assertThat(anomalies)
                .contains("Alignement du texte irrégulier (tampon ou mention ajoutée suspecte).");
    }

    private static List<OcrWord> uniformWords(int count, float confidence) {
        List<OcrWord> words = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int line = i / 6;
            int column = i % 6;
            words.add(word(column * 80, 100 + line * 40, 60, 20, confidence));
        }
        return words;
    }

    private static OcrWord word(int x, int y, int width, int height, float confidence) {
        return new OcrWord("mot", confidence, x, y, width, height);
    }
}
