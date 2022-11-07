package com.amazon.green.book.service.webapp.context;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.amazon.environment.platform.api.marketplace.MarketplaceId;
import com.amazon.environment.platform.api.session.CustomerId;
import com.amazon.environment.platform.api.session.SessionId;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;

/**
 * Request context that supplies customer, session, marketplace and ipAddress information.
 * See: https://w.amazon.com/index.php/Horizonte/Get%20Started/DataSourcesBasic#HHorizonteDataSourceAPI
 */
@Getter
public class ListNearbyStoreRequestContext {

    private static final String ORG_X_FORWARDED = "original-x-forwarded-for";
    private static final String X_FORWARDED = "X-Forwarded-For";

    private final String customerId;
    private final String sessionId;
    private final String marketplaceId;
    private final String almBrandId;
    private final String ipAddress;

    /**
     * Constructing RequestContext from HttpServletRequest and almBrandId.
     *
     * @param request    the HttpServletRequest to construct the RequestContext from.
     * @param almBrandId the Amazon Local Market brand Id to construct the RequestContext from.
     */
    public ListNearbyStoreRequestContext(final HttpServletRequest request, final String almBrandId) {
        this.customerId = CustomerId.resolveCurrent().orElse(null);
        this.sessionId = SessionId.resolveCurrent();
        this.marketplaceId = MarketplaceId.resolveCurrent();
        this.almBrandId = almBrandId;

        // OPF sets Original-X-Forwarded-For to the original chain of IPs encountered.
        // See: https://w.amazon.com/index.php/Online_Proxy_Fleet/User_Docs/X-Forwarded-For
        //  and https://w.amazon.com/index.php/Online_Proxy_Fleet/Whitepapers/Original-X-Forwarded-For
        final String xForwardedFor = request.getHeader(X_FORWARDED);
        final String originalXForwardedFor = request.getHeader(ORG_X_FORWARDED);
        this.ipAddress = isBlank(xForwardedFor) ? originalXForwardedFor : xForwardedFor;
    }
}
