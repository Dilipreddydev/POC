package com.amazon.green.book.service.webapp.config;

import static com.amazon.green.book.service.webapp.constants.ServiceConstants.TEST_DOMAIN;

import amazon.platform.config.AppConfig;
import com.amazon.coral.metrics.reporter.ReporterFactory;
import com.amazon.spring.platform.web.application.initializer.spi.WebApplicationInitializerDelegate;
import com.amazon.statsd.client.FallbackStatsdClient;
import com.amazon.statsd.client.NullStatsdClient;
import com.amazon.statsd.client.StatsdClient;
import com.amazon.statsd.client.netty.NettyStatsdClient;
import com.amazon.statsd.reporter.StatsdReporterFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import edu.emory.mathcs.backport.java.util.Collections;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CloudWatch metrics delegate.
 */
public class CloudWatchMetricsDelegate implements WebApplicationInitializerDelegate {

    /**
     * Singleton instance.
     */
    public static final CloudWatchMetricsDelegate INSTANCE = new CloudWatchMetricsDelegate();
    /**
     * Logger instance.
     */
    private static final Logger LOG = LogManager.getLogger(CloudWatchMetricsDelegate.class);
    /**
     * Address of the statsD daemon sidecar where metrics are reported.
     *
     * <p>This address MUST be unresolved at the time of creation in order to prevent a DNS lookup from occurring at the time of
     * construction in environments where the daemon is not available, e.g. desktop, pipeline integration tests, etc. Our metrics fallback
     * predicate will periodically run and determine whether a null metrics client is used or not based on whether the
     * statsd address is resolvable.
     */
    private static final InetSocketAddress STATSD_ADDRESS =
            InetSocketAddress.createUnresolved("metricaggregationcontainer", 8125);
    private static final Predicate<Void> METRICS_FALLBACK_PREDICATE = v -> {
        try {
            // The creation of an InetSocketAddress will trigger a DNS lookup for the given name. If this succeeds,
            // then we'll attempt to publish metrics to the given address. This method of checking also has the benefit
            // of "just working" if the statsd address is changed to a hardcoded IP address (though in that case, we
            // don't need the fallback mechanism at all).
            new InetSocketAddress(STATSD_ADDRESS.getHostString(), STATSD_ADDRESS.getPort());
            return true;
        } catch (Exception e) {
            LOG.warn("Unable to resolve metrics reporting endpoint");
        }
        // Return false in order to fallback to the null client
        return false;
    };

    @Override
    public List<ReporterFactory> configureMetricsReporterFactory() {
        final ReporterFactory reporterFactory = StatsdReporterFactory.builder()
                .client(getStatsdClient())
                .filter(GreenBookServiceCloudWatchReporterFilter.get())
                .build();

        return Collections.singletonList(reporterFactory);
    }

    private StatsdClient getStatsdClient() {
        // Only require StatsdClient in non-"test" stages
        if (TEST_DOMAIN.equals(AppConfig.getDomain())) {
            return new NullStatsdClient();
        }

        return FallbackStatsdClient.builder()
                .preferredClient(NettyStatsdClient.client(STATSD_ADDRESS, new NioEventLoopGroup(1), false))
                .fallbackClient(new NullStatsdClient())
                .fallbackPredicate(METRICS_FALLBACK_PREDICATE)
                .predicatePeriod(1)
                .predicatePeriodUnit(TimeUnit.MINUTES)
                .scheduler(Executors.newSingleThreadScheduledExecutor(
                        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("fallback-statsd-client-%d").build()))
                .build();
    }
}
