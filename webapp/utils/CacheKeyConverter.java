package com.amazon.green.book.service.webapp.utils;

import static com.amazon.green.book.service.webapp.constants.CacheConstants.CACHE_KEY_SEPARATOR;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheKeyConverter {

    /**
     * Converts the almBrandId and marketPlaceId into cache key of format (almBrandId,marketPlaceId).
     *
     * @param almBrandId an entity that owns regional stores (ALM Stores) like WFM, AFS, Sprouts etc
     * @param marketPlaceId obfuscated marketplace id
     * @return converted cache key string
     */
    public static String convertToCacheKey(final String almBrandId, final String marketPlaceId) {
        return almBrandId + CACHE_KEY_SEPARATOR + marketPlaceId;
    }

    /**
     * Converts the cache key back to separate fields of almBrandId and marketPlaceId.
     *
     * @param cacheKey the cache key to convert
     * @return a String array with almBrandId and marketPlaceId
     */
    public static String[] convertFromCacheKey(final String cacheKey) {
        return cacheKey.split(CACHE_KEY_SEPARATOR);
    }
}
