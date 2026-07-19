package com.laboussole.infrastructure.config;

import com.laboussole.infrastructure.storage.StorageEncryptionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageEncryptionProperties.class)
public class StorageConfig {
}
