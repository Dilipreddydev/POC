package com.amazon.green.book.service.webapp.controller.api;

import static com.amazon.green.book.service.webapp.constants.BrandConstants.AFS_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.OPERATION_PROP;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.SEARCH_STORES_OPERATION;
import static com.amazon.green.book.service.webapp.constants.UrlMapping.SEARCH_STORES_URL;
import static com.amazon.green.book.service.webapp.utils.AlmBrandIdValidator.validateAlmBrandId;
import static com.amazon.green.book.service.webapp.utils.ErrorHandler.handleExecutionException;
import static com.amazon.green.book.service.webapp.utils.StoresFilter.filterStoresByQuery;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import com.amazon.green.book.service.model.StoreInformation;
import com.amazon.green.book.service.webapp.api.AlmStoresServiceApi;
import com.amazon.green.book.service.webapp.cache.StoresForBrandCache;
import com.amazon.green.book.service.webapp.constants.MetricsConstants;
import com.amazon.green.book.service.webapp.context.SearchStoreRequestContext;
import com.amazon.green.book.service.webapp.utils.MetricsEmitter;
import com.amazon.horizonte.spring.annotations.PageType;
import com.amazon.metrics.declarative.WithMetrics;
import com.amazon.metrics.declarative.metrics.ErrorPercentage;
import com.amazon.metrics.declarative.metrics.InclusionMode;
import com.amazon.metrics.declarative.metrics.Prop;
import com.amazon.metrics.declarative.servicemetrics.Availability;
import com.amazon.metrics.declarative.servicemetrics.Timeout;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class SearchStoresController {

    private final AlmStoresServiceApi almStoresServiceApi;
    private final StoresForBrandCache storesForBrandCache;
    private final MetricsEmitter metricsEmitter;

    /**
     * Search ALM stores by search query containing city, state and postal code.
     *
     * @param httpServletRequest the HTTP request
     * @param almBrandId an entity that owns regional stores (ALM Stores) like WFM, AFS, Sprouts etc
     * @param query the search query which may contains city, state and postalCode to search the stores from.
     * @return a list of stores that match search query, empty if no matches.
     */
    @RequestMapping(value = SEARCH_STORES_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @PageType(pageType = MetricsConstants.STORE_FINDER_PAGE_TYPE)
    @Prop(name = OPERATION_PROP, value = SEARCH_STORES_OPERATION)
    @WithMetrics
    @Availability
    @Timeout
    @ErrorPercentage(useDeclared = false, mode = InclusionMode.INCLUDE)
    @ResponseBody
    public List<StoreInformation> searchStores(final HttpServletRequest httpServletRequest,
                                               @RequestParam(value = "almBrandId") final String almBrandId,
                                               @RequestParam(value = "query") final String query) {
        validateAlmBrandId(almBrandId);

        final SearchStoreRequestContext requestContext = new SearchStoreRequestContext(httpServletRequest, almBrandId, query);
        final String marketplaceId = requestContext.getMarketplaceId();
        final String postalCode = requestContext.getPostalCode();
        final String alphaChars = requestContext.getAlphaChars();

        // if a complete 5 digits US postal code is provided, first search stores near(50 miles radius) that postal code
        // then further filter based on provided city and state regardless of postal code
        if (isNumeric(postalCode) && postalCode.length() == 5) {
            // after validation almBrandId could only be either AFS or WFM
            if (AFS_BRAND_ID.equals(almBrandId)) {
                return filterStoresByQuery(almStoresServiceApi.discoverGeoInStoreStores(almBrandId, marketplaceId,
                        null, null, null, postalCode), null, alphaChars);
            } else {
                return filterStoresByQuery(almStoresServiceApi.discoverGeoPickupStores(almBrandId, marketplaceId,
                        null, null, null, postalCode), null, alphaChars);
            }
        }

        try {
            return filterStoresByQuery(storesForBrandCache.getAllStoresForBrand(almBrandId, marketplaceId), postalCode, alphaChars);
        } catch (ExecutionException executionException) {
            throw handleExecutionException(executionException, metricsEmitter);
        }
    }
}
