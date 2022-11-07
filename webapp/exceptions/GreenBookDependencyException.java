package com.amazon.green.book.service.webapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates an error with a dependency.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class GreenBookDependencyException extends RuntimeException {

    /**
     * Constructs an exception with message.
     *
     * @param message Exception message
     */
    public GreenBookDependencyException(final String message) {
        super(message);
    }

    /**
     * Constructs an exception with message and cause.
     *
     * @param message Exception message
     * @param cause   Exception throwable
     */
    public GreenBookDependencyException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
