package com.amazon.green.book.service.webapp.config;

import amazon.platform.config.AppConfig;
import com.amazon.green.book.service.webapp.bindings.ServiceStage;
import org.springframework.context.annotation.Bean;

public class EnvironmentConfig {
    /**
     * Service stage provider.
     *
     * @return Service stage
     */
    @Bean
    @ServiceStage
    public String provideServiceStage() {
        return AppConfig.getDomain();
    }
}
