package com.laboussole.infrastructure.config;

import com.laboussole.infrastructure.satellite.Sentinel2Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(Sentinel2Properties.class)
public class SatelliteAcquisitionConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
