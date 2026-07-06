package com.laboussole.infrastructure.ocr;

import com.laboussole.domain.model.parcel.DocumentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OcrTextFieldExtractorTest {

    private static final String TF_SAMPLE = """
            RÉPUBLIQUE DU MALI
            Un Peuple - Un But - Une Foi
            DIRECTION NATIONALE DES DOMAINES ET DU CADASTRE

            TITRE FONCIER N° 12345/BKO
            PROPRIÉTAIRE : MOUSSA TRAORÉ
            SUPERFICIE : 2,5 HECTARES
            LOCALISATION : BAMAKO, COMMUNE IV
            """;

    @Test
    void extractsTitleNumberOwnerAndSurfaceFromFrenchTitleDocument() {
        var fields = OcrTextFieldExtractor.extract(TF_SAMPLE);

        assertThat(fields.titleNumber()).contains("12345/BKO");
        assertThat(fields.ownerName()).contains("MOUSSA TRAORE");
        assertThat(fields.surfaceAreaHectares()).contains(new BigDecimal("2.5"));
    }

    @Test
    void convertsSquareMetresToHectares() {
        var fields = OcrTextFieldExtractor.extract("SUPERFICIE : 25000 M2");

        assertThat(fields.surfaceAreaHectares()).contains(new BigDecimal("2.5000"));
    }

    @Test
    void toleratesShortTfAbbreviation() {
        var fields = OcrTextFieldExtractor.extract("TF N° ML-2024-0042 delivre a Kati");

        assertThat(fields.titleNumber()).contains("ML-2024-0042");
    }

    @Test
    void absentFieldsStayEmptyWithoutError() {
        var fields = OcrTextFieldExtractor.extract("Document sans les champs attendus");

        assertThat(fields.titleNumber()).isEmpty();
        assertThat(fields.ownerName()).isEmpty();
        assertThat(fields.surfaceAreaHectares()).isEmpty();
    }

    @Test
    void missingTitleNumberOnTitleDocumentIsFlaggedInFrench() {
        var fields = OcrTextFieldExtractor.extract("Document illisible");

        var anomalies = OcrTextFieldExtractor.missingFieldAnomalies(fields, DocumentType.TF);

        assertThat(anomalies)
                .containsExactly("Numéro de titre foncier illisible ou absent du document.");
    }

    @Test
    void noMissingFieldAnomalyForNonTitleDocuments() {
        var fields = OcrTextFieldExtractor.extract("Plan de bornage sans numero");

        assertThat(OcrTextFieldExtractor.missingFieldAnomalies(fields, DocumentType.PLAN)).isEmpty();
    }
}
