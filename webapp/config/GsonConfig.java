package com.amazon.green.book.service.webapp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;

public class GsonConfig {

    /**
     * Gson for serialization and de-serialization.
     * @return gson
     */
    @Bean
    public Gson getGson() {
        return new GsonBuilder().create();
    }
}
