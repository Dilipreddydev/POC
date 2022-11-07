package com.amazon.green.book.service.webapp.controller.api;

import static com.amazon.green.book.service.webapp.constants.BrandConstants.AFS_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.LIST_NEARBY_STORES_OPERATION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.OPERATION_PROP;
import static com.amazon.green.book.service.webapp.constants.UrlMapping.LIST_NEARBY_STORES_URL;
import static com.amazon.green.book.service.webapp.utils.AlmBrandIdValidator.validateAlmBrandId;

import com.amazon.green.book.service.model.StoreInformation;
import com.amazon.green.book.service.webapp.api.AlmStoresServiceApi;
import com.amazon.green.book.service.webapp.constants.MetricsConstants;
import com.amazon.green.book.service.webapp.context.ListNearbyStoreRequestContext;
import com.amazon.horizonte.spring.annotations.PageType;
import com.amazon.metrics.declarative.WithMetrics;
import com.amazon.metrics.declarative.metrics.ErrorPercentage;
import com.amazon.metrics.declarative.metrics.InclusionMode;
import com.amazon.metrics.declarative.metrics.Prop;
import com.amazon.metrics.declarative.servicemetrics.Availability;
import com.amazon.metrics.declarative.servicemetrics.Timeout;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
public class ListNearbyStoresController {

    private final AlmStoresServiceApi almStoresServiceApi;

    /**
     * Discover nearby ALM stores from the customer info.
     *
     * @param httpServletRequest the HTTP request containing ip address
     * @param almBrandId         an entity that owns regional stores (ALM Stores) like WFM, AFS, Sprouts etc
     * @return a list of nearby stores
     */
    @RequestMapping(value = LIST_NEARBY_STORES_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @PageType(pageType = MetricsConstants.STORE_FINDER_PAGE_TYPE)
    @Prop(name = OPERATION_PROP, value = LIST_NEARBY_STORES_OPERATION)
    @WithMetrics
    @Availability
    @Timeout
    @ErrorPercentage(useDeclared = false, mode = InclusionMode.INCLUDE)
    @ResponseBody
    public List<StoreInformation> listNearbyStores(final HttpServletRequest httpServletRequest,
                                                   @RequestParam(value = "almBrandId") final String almBrandId) {

        final ListNearbyStoreRequestContext requestContext = new ListNearbyStoreRequestContext(httpServletRequest, almBrandId);
        final String marketplaceId = requestContext.getMarketplaceId();
        final String sessionId = requestContext.getSessionId();
        final String customerId = requestContext.getCustomerId();
        final String ipAddress = requestContext.getIpAddress();

        validateAlmBrandId(almBrandId);

        // after validation almBrandId could only be either AFS or WFM
        if (AFS_BRAND_ID.equals(almBrandId)) {
            // AFS in-store stores are defined here: https://tiny.amazon.com/thjc87ul/codeamazpackALMStreemainreso
            return almStoresServiceApi
                    .discoverGeoInStoreStores(almBrandId, marketplaceId, customerId, sessionId, ipAddress, null);
        } else {
            // WFM pick-up stores are defined here: https://tiny.amazon.com/zgzkaqn5/codeamazpackALMStreemainreso
            return almStoresServiceApi
                    .discoverGeoPickupStores(almBrandId, marketplaceId, customerId, sessionId, ipAddress, null);
        }
    }
}
