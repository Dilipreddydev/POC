package com.amazon.green.book.service.webapp.controller.core;

import static com.amazon.green.book.service.webapp.constants.BrandConstants.AFS_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.GET_STORE_FINDER_OPERATION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.OPERATION_PROP;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.STORE_FINDER_PAGE_TYPE;
import static com.amazon.green.book.service.webapp.constants.PageConstants.ALM_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.PageConstants.DEVICE_TYPE;
import static com.amazon.green.book.service.webapp.constants.PageConstants.PAGE_TITLE;
import static com.amazon.green.book.service.webapp.constants.PageConstants.SERVICE_STAGE;
import static com.amazon.green.book.service.webapp.constants.PageConstants.STORE_INFORMATION_LIST;
import static com.amazon.green.book.service.webapp.constants.PageConstants.SUCCESS_PAGE_NAME;
import static com.amazon.green.book.service.webapp.constants.UrlMapping.GET_STORE_FINDER_URL;
import static com.amazon.green.book.service.webapp.utils.AlmBrandIdValidator.validateAlmBrandId;

import com.amazon.green.book.service.model.StoreInformation;
import com.amazon.green.book.service.webapp.api.AlmStoresServiceApi;
import com.amazon.green.book.service.webapp.bindings.ServiceStage;
import com.amazon.green.book.service.webapp.context.StoreFinderRequestContext;
import com.amazon.horizonte.csrf.annotations.DisableCsrfInterceptors;
import com.amazon.horizonte.raw.server.spring.params.RawParams;
import com.amazon.horizonte.spring.annotations.PageType;
import com.amazon.horizonte.utils.HorizonteUtils;
import com.amazon.metrics.declarative.WithMetrics;
import com.amazon.metrics.declarative.metrics.ErrorPercentage;
import com.amazon.metrics.declarative.metrics.InclusionMode;
import com.amazon.metrics.declarative.metrics.Prop;
import com.amazon.metrics.declarative.servicemetrics.Availability;
import com.amazon.metrics.declarative.servicemetrics.Timeout;
import com.amazon.reacttoolkit.injector.annotation.ReactToolkitAssets;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for StoreFinder widget.
 */
@Controller
public class StoreFinderController {

    static final String AFS_PAGE_TITLE = "Amazon.com: Amazon Fresh";
    static final String WFM_PAGE_TITLE = "Amazon.com: Whole Foods Market";
    private static final String ASSET_PACKAGE_NAME = "GreenBookWebApp";

    private final AlmStoresServiceApi almStoresServiceApi;
    private final Gson gson;
    private final String serviceStage;

    /**
     * Construct a create StoreFinderController instance.
     *
     * @param almStoresServiceApi AlmStoreService API
     * @param gson                gson
     * @param serviceStage        service stage
     */
    @Inject
    public StoreFinderController(final AlmStoresServiceApi almStoresServiceApi,
                                 final Gson gson,
                                 @ServiceStage final String serviceStage) {
        this.almStoresServiceApi = almStoresServiceApi;
        this.gson = gson;
        this.serviceStage = serviceStage;
    }
optout.horizonte.SecureByDefaultCSRFHeaderProtection=false

    /**
     * Core Spring Controller for StoreFinder Widget.
     *
     * @param rawParams RAW Remote Accessible Widget parameters.
     * @return Spring ModelAndView object with error message or List of StoreInformation.
     */
    @ReactToolkitAssets(assets = {ASSET_PACKAGE_NAME})
    @RequestMapping(GET_STORE_FINDER_URL)
    @PageType(pageType = STORE_FINDER_PAGE_TYPE)
    @Prop(name = OPERATION_PROP, value = GET_STORE_FINDER_OPERATION)
    @WithMetrics
    @Availability
    @Timeout
    @ErrorPercentage(useDeclared = false, mode = InclusionMode.INCLUDE)
    @ResponseBody
    // This is read only end point and uses RAW. See: https://skb.highcastle.a2z.com/implementations/224
    @DisableCsrfInterceptors
    public ModelAndView execute(@RawParams final Map<String, String[]> rawParams) {

        // Convert the rawParams(String, String[]) into HashMap of (String,String)
        final Map<String, String> rawParamsMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : rawParams.entrySet()) {
            rawParamsMap.put(entry.getKey(), HorizonteUtils.getParameter(rawParams, entry.getKey()));
        }

        final StoreFinderRequestContext requestContext = new StoreFinderRequestContext(rawParamsMap);
        final String marketplaceId = requestContext.getMarketplaceId();
        final String sessionId = requestContext.getSessionId();
        final String customerId = requestContext.getCustomerId();
        final String ipAddress = requestContext.getIpAddress();
        final String almBrandId = requestContext.getAlmBrandId();
        final String deviceType = requestContext.getDeviceType();

        List<StoreInformation> storeInformationList;
        String pageTitle;

        validateAlmBrandId(almBrandId);

        // after validation almBrandId could only be either AFS or WFM
        if (AFS_BRAND_ID.equals(almBrandId)) {
            // AFS in-store stores are defined here: https://tiny.amazon.com/thjc87ul/codeamazpackALMStreemainreso
            storeInformationList = almStoresServiceApi.discoverGeoInStoreStores(almBrandId, marketplaceId, customerId,
                    sessionId, ipAddress, null);
            pageTitle = AFS_PAGE_TITLE;
        } else {
            // WFM pick-up stores are defined here: https://tiny.amazon.com/zgzkaqn5/codeamazpackALMStreemainreso
            storeInformationList = almStoresServiceApi.discoverGeoPickupStores(almBrandId, marketplaceId, customerId,
                    sessionId, ipAddress, null);
            pageTitle = WFM_PAGE_TITLE;
        }

        final ModelAndView modelAndView = new ModelAndView(SUCCESS_PAGE_NAME);
        modelAndView.addObject(STORE_INFORMATION_LIST, gson.toJson(storeInformationList));
        modelAndView.addObject(DEVICE_TYPE, deviceType);
        modelAndView.addObject(ALM_BRAND_ID, almBrandId);
        modelAndView.addObject(SERVICE_STAGE, serviceStage);
        modelAndView.addObject(PAGE_TITLE, pageTitle);
        return modelAndView;
    }
}
