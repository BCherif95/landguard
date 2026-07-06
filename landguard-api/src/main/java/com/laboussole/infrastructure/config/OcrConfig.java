package com.laboussole.infrastructure.config;

import com.laboussole.infrastructure.ocr.OcrProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OcrProperties.class)
class OcrConfig {
}
