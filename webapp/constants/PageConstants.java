package com.amazon.green.book.service.webapp.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PageConstants {

    // Page name
    public static final String SUCCESS_PAGE_NAME = "storeFinder.jsp";
    public static final String BRAND_ID_ERROR_PAGE_NAME = "almBrandIdError.jsp";

    // ModelAndView attribute names
    public static final String ALM_BRAND_ID = "almBrandId";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String SERVICE_STAGE = "serviceStage";
    public static final String STORE_INFORMATION_LIST = "storeInformationList";
    public static final String PAGE_TITLE = "pageTitle";
}
