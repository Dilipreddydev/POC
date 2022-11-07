package com.amazon.green.book.service.webapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates an error with an input or inputs.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class GreenBookInvalidInputException extends RuntimeException {

    /**
     * Constructs an exception with message.
     *
     * @param message Exception message
     */
    public GreenBookInvalidInputException(final String message) {
        super(message);
    }

    /**
     * Constructs an exception with message and cause.
     *
     * @param message Exception message
     * @param cause   Exception throwable
     */
    public GreenBookInvalidInputException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
