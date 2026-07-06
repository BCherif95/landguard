package com.laboussole.infrastructure.ocr;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tesseract engine settings.
 *
 * @param tessdataPath directory containing the trained language files
 *                     ({@code fra.traineddata}…); empty means the engine's
 *                     default lookup ({@code TESSDATA_PREFIX})
 * @param language     Tesseract language codes, e.g. {@code "fra"} or
 *                     {@code "fra+eng"}
 */
@ConfigurationProperties(prefix = "laboussole.ocr")
public record OcrProperties(String tessdataPath, String language) {
}
