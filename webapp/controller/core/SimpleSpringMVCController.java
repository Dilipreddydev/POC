package com.amazon.green.book.service.webapp.controller.core;

import com.amazon.horizonte.spring.annotations.PageType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is a plain Spring MVC controller to handle requests to "/".
 */
@Controller
@SuppressWarnings({"checkstyle:abbreviationaswordinname"})
public class SimpleSpringMVCController {

    /**
     * Home controller method.
     *
     * @return Spring ModelAndView object
     */
    @RequestMapping("/")
    @PageType(pageType = "PlaceHolderPageType")
    public ModelAndView execute() {
        return new ModelAndView("home.jsp");
    }
}
