package com.amazon.green.book.service.webapp.context;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.isAlpha;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.Validate.isTrue;

import com.amazon.green.book.service.webapp.exceptions.GreenBookInvalidInputException;
import com.amazon.shopping.portal.protocol.Header;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
public class SearchStoreRequestContext {

    public static final String SPACE = " ";

    private static final String ALPHA_NUMERIC_COMMA_SPACE_REGEX = "^[a-zA-Z0-9, ]*$";
    private static final String SPACE_REGEX = "[ ]+";
    private static final String POSTAL_CODE = "postalCode";
    private static final String COMMA = ",";

    private final String marketplaceId;
    private final String almBrandId;
    private String postalCode;

    // This field extracts the alpha chars in raw search query and is later used to match stores by city and state fields
    // in StoresFilter class
    private String alphaChars = "";

    /**
     * Constructing SearchStoreRequestContext from almBrandId and search query.
     *
     * @param request the HttpServletRequest to construct the RequestContext from.
     * @param almBrandId the Amazon Local Market brand Id to construct the RequestContext from.
     * @param query the search query which may contains city, state and postalCode to construct the RequestContext from.
     * @throws GreenBookInvalidInputException if the query is invalid.
     */
    public SearchStoreRequestContext(final HttpServletRequest request,
                                     final String almBrandId,
                                     final String query) throws GreenBookInvalidInputException {
        this.marketplaceId = request.getHeader(Header.X_AMZ_PORTAL_MARKETPLACE_ID.name());
        this.almBrandId = almBrandId;

        // raw query should only contain letter, number, comma and space
        if (!query.matches(ALPHA_NUMERIC_COMMA_SPACE_REGEX)) {
            throw handleInvalidSearchQuery(query, "invalid characters in the query.");
        }

        if (query.trim().length() < 2) {
            throw handleInvalidSearchQuery(query, "Query min length should be at least 2 chars.");
        }

        // This regex breaks query into tokens by 1 or unlimited space, for example
        // " wa seattle          98101" -> [wa, seattle, 98101]
        final String[] queryTokens = query.replace(COMMA, SPACE).trim().split(SPACE_REGEX);

        stream(queryTokens).forEach(token -> {
            if (isAlpha(token)) {
                this.alphaChars += token + SPACE;
            } else if (isNumeric(token)) {
                // we only deal with 5 digits US postalCode format for now
                isAttributeNotSet(POSTAL_CODE, this.postalCode, query);
                this.postalCode = token;
            } else if (!isBlank(token)) {
                // for example, query "abc123 seattle" could trigger this with a token "abc123" that can't be parsed
                // each single token has to be either 100% alphabetic or 100% numeric
                throw handleInvalidSearchQuery(query, "All words in the query should be either 100% alphabetic or 100% numeric.");
            }
            // it is possible to have blank token, just continue the loop to ignore such token
        });

        // for example raw search query like ",,,,,,," could trigger this if clause
        if (isBlank(this.alphaChars) && isBlank(this.postalCode)) {
            throw handleInvalidSearchQuery(query, "All words in the query should be either 100% alphabetic or 100% numeric.");
        }

        // After processing the last alphabetic word, the alphaChars will have a trailing space.
        // For example raw query "seat,wa" will produce alphaChars = "seat wa " because when dealing with "wa",
        // alphaChars = "seat " + "wa" + " ". So at the very end need to trim the trailing space
        this.alphaChars = this.alphaChars.trim();
    }

    // check if an attribute has been set yet
    private void isAttributeNotSet(final String attributeName,
                                   final Object oldValue,
                                   final String query) {
        final String message = attributeName + " has already been set.";
        try {
            isTrue(oldValue == null);
        } catch (IllegalArgumentException ex) {
            throw handleInvalidSearchQuery(query, message);
        }
    }

    // return the exception back to the caller functions to throw in order to reach 100% code coverage in the caller functions
    private GreenBookInvalidInputException handleInvalidSearchQuery(final String query, final String reason) {
        // logs at INFO level because this is a client side 400 error
        log.info("Search Store API received invalid search query: {} because: {}", query, reason);
        return new GreenBookInvalidInputException("Search Store API received invalid search query: " + query + " because " + reason);
    }
}
