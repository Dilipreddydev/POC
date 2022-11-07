package com.amazon.green.book.service.webapp.config;

import static com.amazon.green.book.service.webapp.constants.BrandConstants.AFS_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.BrandConstants.WFM_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.ServiceConstants.DEFAULT_MARKETPLACE_ID;
import static com.amazon.green.book.service.webapp.utils.ErrorHandler.handleExecutionException;
import static com.amazon.green.book.service.webapp.utils.ErrorHandler.handleInterruptedException;
import static com.amazon.green.book.service.webapp.utils.ErrorHandler.handleTimeoutException;

import com.amazon.green.book.service.webapp.api.AlmStoresServiceApi;
import com.amazon.green.book.service.webapp.cache.StoresForBrandCache;
import com.amazon.green.book.service.webapp.utils.MetricsEmitter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MetricsProviderConfig.class})
public class StoresForBrandCacheConfig {

    /**
     * StoresForBrandCache bean.
     *
     * @param almStoresServiceApi AlmStoreService API
     * @param executorService ExecutorService used by cache async reload/refresh
     * @param metricsEmitter MetricsEmitter used to log occurrence of cache exception
     * @return StoresForBrandCache
     */
    @Bean
    public StoresForBrandCache getStoresForBrandCache(final AlmStoresServiceApi almStoresServiceApi,
                                                      final ExecutorService executorService,
                                                      final MetricsEmitter metricsEmitter) {
        final StoresForBrandCache storesForBrandCache = new StoresForBrandCache(almStoresServiceApi, executorService);

        try {
            // Pre-warm the cache for WFM and AFS stores in US Marketplace
            final CompletableFuture<Void> wfmFuture = CompletableFuture.runAsync(new Runnable() {

                @SneakyThrows
                @Override
                public void run() {
                    storesForBrandCache.getAllStoresForBrand(WFM_BRAND_ID, DEFAULT_MARKETPLACE_ID);
                }
            });

            final CompletableFuture<Void> afsFuture = CompletableFuture.runAsync(new Runnable() {

                @SneakyThrows
                @Override
                public void run() {
                    storesForBrandCache.getAllStoresForBrand(AFS_BRAND_ID, DEFAULT_MARKETPLACE_ID);
                }
            });

            wfmFuture.get(3, TimeUnit.SECONDS);
            afsFuture.get(3, TimeUnit.SECONDS);
        } catch (ExecutionException executionException) {
            throw handleExecutionException(executionException, metricsEmitter);
        } catch (InterruptedException interruptedException) {
            throw handleInterruptedException(interruptedException, metricsEmitter);
        } catch (TimeoutException timeoutException) {
            throw handleTimeoutException(timeoutException,metricsEmitter);
        }
        return storesForBrandCache;
    }
}
