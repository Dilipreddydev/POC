package com.amazon.green.book.service.webapp.config;

import com.amazon.coral.metrics.Metrics;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazon.environment.platform.api.metrics.CoralMetrics;
import com.amazon.metrics.declarative.DefaultMetricsManager;
import com.amazon.metrics.declarative.MetricsFactoriesHelper;
import com.amazon.metrics.declarative.MetricsManager;
import com.amazon.metrics.declarative.aspectj.MetricMethodAspect;
import com.amazon.metrics.declarative.servicemetrics.aspectj.ServiceMetricsMethodAspect;
import com.amazon.metrics.executor.MetricsAwareExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.aspectj.lang.Aspects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Declarative Metrics support beans.
 */
@Configuration
public class MetricsProviderConfig {

    /**
     * MetricsFactory Bean.
     *
     * @return MetricsFactory
     */
    @Bean(name = "metricsFactory")
    public MetricsFactory getHorizonteMetricsFactory() {
        return new MetricsFactory() {

            @Override
            public Metrics newMetrics() {
                return CoralMetrics.resolveCurrent().newMetrics();
            }
        };
    }

    /**
     * MetricsManager Bean.
     *
     * @param metricsFactory **is metricsFactory**
     * @return MetricsManager
     */
    @Bean
    public MetricsManager getMetricsManager(MetricsFactory metricsFactory) {
        return new DefaultMetricsManager(metricsFactory);
    }

    /**
     * Method metrics aspect bean.
     *
     * @param metricsManager **is MetricsManager**
     * @return Down-casted {@code MetricMethodAspect}.
     */
    @Bean
    public Object methodMetricAspect(MetricsManager metricsManager) {
        final MetricMethodAspect metricAspect = Aspects.aspectOf(MetricMethodAspect.class);
        metricAspect.setMetricsManager(metricsManager);
        metricAspect.setFactories(MetricsFactoriesHelper.defaultMetricFactories());
        return metricAspect;
    }

    /**
     * Service metrics aspect bean.
     *
     * @param metricsManager **is MetricsManager**
     * @return Down-casted {@code ServiceMetricMethodAspect}.
     */
    @Bean
    public Object serviceMetricsAspect(MetricsManager metricsManager) {
        final ServiceMetricsMethodAspect serviceMetricsMethodAspect = Aspects.aspectOf(ServiceMetricsMethodAspect.class);
        serviceMetricsMethodAspect.setMetricsManager(metricsManager);
        return serviceMetricsMethodAspect;
    }

    /**
     * ExecutorService bean used by async cache reload.
     *
     * @param metricsManager **is MetricsManager**
     * @return ExecutorService
     */
    @Bean
    public ExecutorService getMetricsAwareExecutor(MetricsManager metricsManager) {
        // This is needed per https://w.amazon.com/index.php/Coral/Community/DeclarativeCoralMetrics#Multi-threading
        // Our stores for branch cache has async cache reload/refresh implementation which calls findAllStoresForBrand method
        // that has declarative coral metrics annotations.
        // Doing this can avoid error https://paste.amazon.com/show/suanran/1635625190 during cache reload/refresh
        return new MetricsAwareExecutor(Executors.newCachedThreadPool(), metricsManager);
    }
}