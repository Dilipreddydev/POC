package com.amazon.green.book.service.webapp.config;

import static com.amazon.green.book.service.webapp.constants.UrlMapping.SEARCH_STORES_URL;

import amazon.platform.config.AppConfig;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer {
    static final String STORYBOOK_ORIGIN = "http://localhost:6007";
    static final String ZHANWZ_STORYBOOK_ORIGIN = "http://zhanwz.corp.amazon.com:6007";
    static final String PKKAPAKO_STORYBOOK_ORIGIN = "http://kapakos.aka.corp.amazon.com:6007";

    /**
     * Config CORS access for non-prod stage.
     *
     * @param registry cors registry
     */
    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        if (AppConfig.isProduction()) {
            return;
        }

        registry.addMapping(SEARCH_STORES_URL).allowedOrigins(STORYBOOK_ORIGIN, ZHANWZ_STORYBOOK_ORIGIN, PKKAPAKO_STORYBOOK_ORIGIN);
    }
}
