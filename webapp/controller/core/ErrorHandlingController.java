package com.amazon.green.book.service.webapp.controller.core;

import static com.amazon.green.book.service.webapp.constants.MetricsConstants.ERROR_404_OPERATION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.ERROR_404_PAGE_TYPE;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.ERROR_500_OPERATION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.ERROR_500_PAGE_TYPE;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.OPERATION_PROP;
import static com.amazon.green.book.service.webapp.constants.UrlMapping.INTERNAL_SERVER_ERROR_URL;
import static com.amazon.green.book.service.webapp.constants.UrlMapping.PAGE_NOT_FOUND_URL;

import com.amazon.environment.platform.api.request.IsInternal;
import com.amazon.horizonte.spring.annotations.PageType;
import com.amazon.metrics.declarative.WithMetrics;
import com.amazon.metrics.declarative.metrics.ErrorPercentage;
import com.amazon.metrics.declarative.metrics.InclusionMode;
import com.amazon.metrics.declarative.metrics.Prop;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class is for error handling. Replace the messages and error.jsp file with content you want to show to your users. Error handling in
 * Horizonte: https://w.amazon.com/bin/view/Horizonte/Dive_Deeper/ExceptionHandling The error messages will only be displayed for internal
 * requests.
 */
@Controller
public class ErrorHandlingController {

    private static final String ERROR_PAGE = "error.jsp";

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String LINK = "link";

    private static final String ERROR_HEADER = "Oops, something has gone wrong.";
    private static final String ERROR_404 = "Error 404. The page handler is not found.";
    private static final String ERROR_500 = "Error 500. An unexpected error has occurred.";
    private static final String DOCUMENTATION_LINK =
            "<a href=\"https://w.amazon.com/bin/view/Horizonte/Dive_Deeper/ExceptionHandling\">"
                    + "Documentation about Horizonte's error handling</a>";

    /**
     * No page handler found.
     *
     * @return ModelAndView
     */
    @RequestMapping(PAGE_NOT_FOUND_URL)
    @PageType(pageType = ERROR_404_PAGE_TYPE)
    @Prop(name = OPERATION_PROP, value = ERROR_404_OPERATION)
    @WithMetrics
    @ErrorPercentage(useDeclared = false, mode = InclusionMode.INCLUDE)
    public ModelAndView noHandlerFound() {
        return generateView(ERROR_404);
    }

    /**
     * Internal server error.
     *
     * @return ModelAndView
     */
    @RequestMapping(INTERNAL_SERVER_ERROR_URL)
    @PageType(pageType = ERROR_500_PAGE_TYPE)
    @Prop(name = OPERATION_PROP, value = ERROR_500_OPERATION)
    @WithMetrics
    @ErrorPercentage(useDeclared = false, mode = InclusionMode.INCLUDE)
    public ModelAndView internalError() {
        return generateView(ERROR_500);
    }

    /**
     * This is a helper function that generates the required ModelAndView.
     *
     * @return ModelAndView
     */
    private ModelAndView generateView(String errorCode) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(ERROR_PAGE);
        if (IsInternal.resolveCurrent()) {
            mav.addObject(LINK, DOCUMENTATION_LINK);
            mav.addObject(TITLE, ERROR_HEADER);
            mav.addObject(MESSAGE, errorCode);
        }
        return mav;
    }
}
