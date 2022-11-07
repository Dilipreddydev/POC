package com.amazon.green.book.service.webapp.utils;

import static com.amazon.green.book.service.webapp.constants.MetricsConstants.EXECUTION_EXCEPTION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.INTERRUPTED_EXCEPTION;
import static com.amazon.green.book.service.webapp.constants.MetricsConstants.TIMEOUT_EXCEPTION;

import com.amazon.green.book.service.webapp.exceptions.GreenBookDependencyException;
import com.sun.jdi.InternalException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class ErrorHandler {

    /**
     * Handles ExecutionException and emit metric count.
     *
     * @param executionException ExecutionException to handle
     * @param metricsEmitter MetricsEmitter to emit the metric count
     * @return RuntimeException back to the caller function to throw
     */
    public static RuntimeException handleExecutionException(final ExecutionException executionException,
                                                            final MetricsEmitter metricsEmitter) {
        metricsEmitter.emitCount(EXECUTION_EXCEPTION, 1);
        final Throwable cause = executionException.getCause();

        log.error("Encountered ExecutionException caused by a {}, throwing the cause instead.",
                cause.getClass().getSimpleName(),
                executionException);

        // return the exception back to the caller functions to throw in order to reach 100% code coverage in the caller functions
        if (cause instanceof GreenBookDependencyException) {
            return (GreenBookDependencyException) cause;
        } else if (cause instanceof RuntimeException) {
            return (RuntimeException) cause;
        } else {
            return (InternalException) new InternalException(cause.getMessage()).initCause(cause);
        }
    }

    /**
     * Handles InterruptedException and emit metric count.
     *
     * @param interruptedException InterruptedException to handle
     * @param metricsEmitter MetricsEmitter to emit the metric count
     * @return InternalException back to the caller function to throw
     */
    public static InternalException handleInterruptedException(final InterruptedException interruptedException,
                                                               final MetricsEmitter metricsEmitter) {
        metricsEmitter.emitCount(INTERRUPTED_EXCEPTION, 1);
        log.error("Encountered InterruptedException, throwing as InternalException instead", interruptedException);

        // return the exception back to the caller functions to throw in order to reach 100% code coverage in the caller functions
        return (InternalException) new InternalException(interruptedException.getMessage()).initCause(interruptedException);
    }

    /**
     * Handles TimeoutException and emit metric count.
     *
     * @param timeoutException TimeoutException to handle exception
     * @param metricsEmitter MetricsEmitter to emit the metric count
     * @return InternalException back to the caller function to throw
     */
    public static InternalException handleTimeoutException(final TimeoutException timeoutException,
                                                           final MetricsEmitter metricsEmitter) {
        metricsEmitter.emitCount(TIMEOUT_EXCEPTION, 3);
        log.error("Encountered TimeoutException, throwing as TimeoutException  instead", timeoutException);

        // return the exception back to the caller functions to throw in order to reach 100% code coverage in the caller functions
        return (InternalException) new InternalException(timeoutException.getMessage()).initCause(timeoutException);
    }
}
