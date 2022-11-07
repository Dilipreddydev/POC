package com.amazon.green.book.service.webapp.context;

import static com.amazon.core.platform.raw.runtime.constants.RawParameters.CUSTOMER_ID;
import static com.amazon.core.platform.raw.runtime.constants.RawParameters.MARKETPLACE_ID;
import static com.amazon.core.platform.raw.runtime.constants.RawParameters.SESSION_ID;
import static com.amazon.green.book.service.webapp.constants.RawParamsConstants.ALM_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.RawParamsConstants.DEVICE_TYPE;
import static com.amazon.green.book.service.webapp.constants.RawParamsConstants.IP_ADDRESS;

import java.util.Map;
import lombok.Getter;

@Getter
public class StoreFinderRequestContext {

    private static final String DEFAULT_SEATTLE_IP_ADDRESS = "69.167.22.235";

    private final String customerId;
    private final String sessionId;
    private final String marketplaceId;
    private final String almBrandId;
    private final String ipAddress;
    private final String deviceType;

    /**
     * Constructing RequestContext from RAW parameters.
     *
     * @param rawParamsMap the RAW parameters Map to construct the RequestContext from.
     */
    public StoreFinderRequestContext(final Map<String, String> rawParamsMap) {
        this.customerId = rawParamsMap.get(CUSTOMER_ID);
        this.sessionId = rawParamsMap.get(SESSION_ID);
        this.marketplaceId = rawParamsMap.get(MARKETPLACE_ID);
        this.almBrandId = rawParamsMap.get(ALM_BRAND_ID);
        this.ipAddress = rawParamsMap.getOrDefault(IP_ADDRESS, DEFAULT_SEATTLE_IP_ADDRESS);
        // DeviceType logic is defined here:
        // https://tiny.amazon.com/31iguv2z/codeamazpackFresblobe55csrc
        // https://tiny.amazon.com/d93eiril/codeamazpackCoreblobMV53src
        this.deviceType = rawParamsMap.get(DEVICE_TYPE);
    }
}
