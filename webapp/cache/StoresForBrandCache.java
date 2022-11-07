package com.amazon.green.book.service.webapp.cache;

import static com.amazon.green.book.service.webapp.constants.CacheConstants.CACHE_TTL_HOURS;
import static com.amazon.green.book.service.webapp.utils.CacheKeyConverter.convertToCacheKey;
import static com.google.common.cache.CacheLoader.asyncReloading;

import com.amazon.green.book.service.model.StoreInformation;
import com.amazon.green.book.service.webapp.api.AlmStoresServiceApi;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class StoresForBrandCache {

    // Local loading cache that maps String <almBrandId,marketPlaceId> to List<StoreInformation>
    private final LoadingCache<String, List<StoreInformation>> storesCache;

    /**
     * Constructor for the StoresForBrandCache.
     *
     * @param almStoresServiceApi the AlmStoresServiceApi used to interact with AlmStoresService
     * @param executorService ExecutorService
     */
    public StoresForBrandCache(final AlmStoresServiceApi almStoresServiceApi,
                               final ExecutorService executorService) {

        storesCache = CacheBuilder
                .newBuilder()
                .refreshAfterWrite(CACHE_TTL_HOURS, TimeUnit.HOURS)
                .build(asyncReloading(new StoresForBrandCacheLoader(almStoresServiceApi), executorService));
    }

    public List<StoreInformation> getAllStoresForBrand(final String almBrandId, final String marketPlaceId) throws ExecutionException {
        return storesCache.get(convertToCacheKey(almBrandId, marketPlaceId));
    }

    @VisibleForTesting
    long getCacheSize() {
        return storesCache.size();
    }

    @VisibleForTesting
    List<StoreInformation> getIfPresent(final String almBrandId, final String marketPlaceId) {
        return storesCache.getIfPresent(convertToCacheKey(almBrandId, marketPlaceId));
    }
}
