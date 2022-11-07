package com.amazon.green.book.service.webapp.config;

import com.amazon.coral.community.metrics.Helper.CoralSensorMetricsRecorder;
import com.amazon.coral.metrics.helper.MetricsHelper;
import com.amazon.horizonte.initialization.delegate.HorizonteAmazonWebInitializerDelegate;
import com.amazon.spring.platform.runtime.MetricsFactory;
import com.amazon.spring.platform.runtime.PlatformWebApplicationInitializerBuilder;
import com.amazon.spring.platform.web.application.initializer.api.PlatformWebApplicationInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.web.WebApplicationInitializer;
import software.amazon.codeguruprofilerjavaagent.Profiler;

/**
 * The standard implementation of the {@code PlatformWebApplicationInitializer} interface that the platform uses to initialize web
 * application. This implementation uses as ordered list of delegates for extension points. The potential extension points are described on
 * the {@code WebApplicationInitializerDelegate} interface. This list of delegates is always iterated in reverse order so a particular
 * extension point for the first` delegate will always get called after the same extension point for the delegate next in the list.
 */
public class HorizonteWebApplicationInitializer implements WebApplicationInitializer {

    private static final int METRICS_SENSOR_POLLING_INTERVAL_IN_SECONDS = 10;
    private static final String PROGRAM = "GreenBookService";
    private static final String PROFILER_NAME = "GreenBookServiceProfiler";
    private static final String MARKETPLACE = "UNKNOWN";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        PlatformWebApplicationInitializer initializer = PlatformWebApplicationInitializerBuilder.getBuilder()
                .delegate(HorizonteAmazonWebInitializerDelegate.INSTANCE)
                .delegate(CloudWatchMetricsDelegate.INSTANCE)
                .additionalRootConfigClass(CustomRootConfig.class)
                .useDefaultUncaughtExceptionHandler(true)
                .build();

        initializer.onStartup(servletContext);

        //Starting the profiler thread
        new Profiler.Builder().profilingGroupName(PROFILER_NAME).build()
                .start();

        // Record JMX metrics
        new CoralSensorMetricsRecorder(
                MetricsFactory.resolveCurrent(),
                METRICS_SENSOR_POLLING_INTERVAL_IN_SECONDS,
                PROGRAM,
                MARKETPLACE,
                MetricsHelper.getPlatformDefaultSensors());
    }
}
