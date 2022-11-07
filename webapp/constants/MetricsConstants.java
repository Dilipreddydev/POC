package com.amazon.green.book.service.webapp.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MetricsConstants {

    public static final String OPERATION_PROP = "Operation";

    public static final String GET_STORE_FINDER_OPERATION = "getStoreFinder";
    public static final String LIST_NEARBY_STORES_OPERATION = "listNearbyStores";
    public static final String SEARCH_STORES_OPERATION = "searchStores";
    public static final String DISCOVER_IN_STORE_STORES_OPERATION = "discoverInStoreStores";
    public static final String DISCOVER_PICKUP_STORES_OPERATION = "discoverPickupStores";
    public static final String FIND_STORES_FOR_BRAND_OPERATION = "findStoresForBrand";

    public static final String STORE_FINDER_PAGE_TYPE = "StoreFinder";

    public static final String ALM_STORE_SERVICE = "ALM_STORE_SERVICE";

    public static final String ERROR_404_OPERATION = "NotFound";
    public static final String ERROR_404_PAGE_TYPE = "Error404";

    public static final String ERROR_500_OPERATION = "internalError";
    public static final String ERROR_500_PAGE_TYPE = "Error500";

    public static final String DISCOVER_IN_STORE_STORES_NOT_FOUND = "DiscoverGeoInStoreStores.NotFound";
    public static final String DISCOVER_PICKUP_STORES_NOT_FOUND = "DiscoverGeoPickupStores.NotFound";
    public static final String ALM_STORE_LOCATION_NOT_FOUND = "ALMStoreLocation.NotFound";
    public static final String EXECUTION_EXCEPTION = "ExecutionException";
    public static final String INTERRUPTED_EXCEPTION = "InterruptedException";
    public static final String TIMEOUT_EXCEPTION = "TimeoutException";
}
