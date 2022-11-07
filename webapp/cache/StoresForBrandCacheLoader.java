package com.amazon.green.book.service.webapp.cache;

import static com.amazon.green.book.service.webapp.utils.CacheKeyConverter.convertFromCacheKey;

import com.amazon.green.book.service.model.StoreInformation;
import com.amazon.green.book.service.webapp.api.AlmStoresServiceApi;
import com.google.common.cache.CacheLoader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class StoresForBrandCacheLoader extends CacheLoader<String, List<StoreInformation>> {

    private final AlmStoresServiceApi almStoresServiceApi;

    @Override
    public List<StoreInformation> load(final String cacheKey) {
        final String[] cacheKeys = convertFromCacheKey(cacheKey);
        final String almBrandId = cacheKeys[0];
        final String marketPlaceId = cacheKeys[1];
        return almStoresServiceApi.findAllStoresForBrand(almBrandId, marketPlaceId);
    }
}
