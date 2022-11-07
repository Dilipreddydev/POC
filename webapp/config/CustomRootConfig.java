package com.amazon.green.book.service.webapp.config;

import com.amazon.horizonte.browserdata.config.CSMBeanConfiguration;
import com.amazon.horizonte.smoketests.configuration.HorizonteSmokeTestBeanConfiguration;
import com.amazon.reacttoolkit.injector.configuration.ReactToolkitSpringConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;

/**
 * Root spring configuration at Application level. HorizonteSmokeTestBeanConfiguration is imported for configuration and basic functionality
 * verification. Please remove it before going to production.
 */
@Configuration
@ComponentScan({"com.amazon.green.book.service.webapp"})
@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@Import({CSMBeanConfiguration.class, HorizonteSmokeTestBeanConfiguration.class, MetricsProviderConfig.class,
    ReactToolkitSpringConfiguration.class, AlmStoresServiceClientConfig.class, GsonConfig.class,
    EnvironmentConfig.class, StoresForBrandCacheConfig.class, CorsConfig.class})
@Log4j2
public class CustomRootConfig {

}
