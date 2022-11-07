package com.amazon.green.book.service.webapp.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlMapping {

    public static final String GET_STORE_FINDER_URL = "/v0/storeFinder";
    public static final String LIST_NEARBY_STORES_URL = "/v0/api/stores";
    public static final String SEARCH_STORES_URL = "/v0/api/stores/search";
    public static final String PAGE_NOT_FOUND_URL = "/404";
    public static final String INTERNAL_SERVER_ERROR_URL = "/500";
}
