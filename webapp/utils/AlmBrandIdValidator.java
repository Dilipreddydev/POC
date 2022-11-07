package com.amazon.green.book.service.webapp.utils;

import static com.amazon.green.book.service.webapp.constants.BrandConstants.AFS_BRAND_ID;
import static com.amazon.green.book.service.webapp.constants.BrandConstants.WFM_BRAND_ID;

import com.amazon.green.book.service.webapp.exceptions.GreenBookInvalidInputException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class AlmBrandIdValidator {

    /**
     * Validates the input almBrandId to be either AFS or WFM.
     *
     * @param almBrandId the almBrandId to validate
     * @throws GreenBookInvalidInputException if invalid almBrandId
     */
    public static void validateAlmBrandId(final String almBrandId) throws GreenBookInvalidInputException {
        if (!AFS_BRAND_ID.equals(almBrandId) && !WFM_BRAND_ID.equals(almBrandId)) {
            // logs at ERROR level because in production use this error should never happen
            log.error("Received invalid almBrandId: {}", almBrandId);
            throw new GreenBookInvalidInputException("Received invalid almBrandId: " + almBrandId);
        }
    }

}
