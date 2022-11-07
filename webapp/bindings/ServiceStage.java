package com.amazon.green.book.service.webapp.bindings;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * Interface used as an annotation to link a @Provider with a consumer of the service stage: one of (test, prod).
 */
@Target({METHOD, PARAMETER})
@Retention(RUNTIME)
@Qualifier
public @interface ServiceStage {

}