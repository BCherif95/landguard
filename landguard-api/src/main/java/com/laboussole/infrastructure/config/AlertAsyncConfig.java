package com.laboussole.infrastructure.config;

import com.laboussole.infrastructure.notification.AlertNotificationProperties;
import com.laboussole.infrastructure.notification.TwilioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/** Dedicated executor for parallel multi-channel alert dispatch. */
@Configuration
@EnableConfigurationProperties({AlertNotificationProperties.class, TwilioProperties.class})
class AlertAsyncConfig {

    @Bean(name = "alertTaskExecutor")
    Executor alertTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("alert-dispatch-");
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.initialize();
        return executor;
    }
}
