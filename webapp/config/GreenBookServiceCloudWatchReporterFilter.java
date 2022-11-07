package com.amazon.green.book.service.webapp.config;

import com.amazon.hex.naws.metrics.configurations.HexMetrcsFilterConstants;
import com.amazon.statsd.reporter.MetricNameWhitelistFilter;
import com.amazon.statsd.reporter.PropertyWhitelistFilter;
import com.amazon.statsd.reporter.ReporterFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.annotation.NotThreadSafe;


/**
 * Filter the list of metrics/dimensions so we don't overwhelm Cloudwatch. Only dimensions/metrics in the filter will be published.
 */
@NotThreadSafe
public final class GreenBookServiceCloudWatchReporterFilter {

    /**
     * Allowlist of dimension names. Add EXPLICITLY all dimensions that your business logic relies on. See {@link HexMetrcsFilterConstants}
     * documentation for more information. Also see: https://tiny.amazon.com/4n4c61hm/codeamazpackCloublobb3b9src .
     */
    private static final Set<String> DIMENSION_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "ServiceName",
            "Program",
            "Operation",
            "MarketplaceId",
            "REQUEST_METHOD")));

    /**
     * Allowlist of metric names. Add EXPLICITLY all metrics that your business logic depends on. See {@link HexMetrcsFilterConstants}
     * documentation for more information. Also see: https://tiny.amazon.com/uzle507c/codeamazpackCloublobb3b9src .
     */
    private static final Set<String> METRIC_NAMES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "Time",
            "Timeout:Critical",
            "TotalTime",
            "HZ5UTF8",
            "Availability:Critical",
            "Backoff:Critical",
            "BatchSize",
            "Latency",
            // JMX Metrics
            "FileDescriptorUse",
            "Threads",
            "HeapMemoryUse",
            "HeapMemoryAfterGCUse",
            "NonHeapMemoryUse",
            "GarbageCollection",
            "ErrorPercentage")));

    private GreenBookServiceCloudWatchReporterFilter() {
    }

    /**
     * Get reporter filter.
     *
     * @return reporter filter
     */
    public static ReporterFilter get() {
        // These allowed dimensions help monitor service health and performance. Please avoid removing them.
        // Feel free to add new metrics or dimensions as needed per your requirements.
        Set<String> dimensions = new HashSet<>(HexMetrcsFilterConstants.HEX_MINIMAL_DIMENSION_NAMES_FILER);
        dimensions.addAll(DIMENSION_NAMES);

        // These allowed dimensions help monitor service health and performance. Please avoid removing them.
        // Feel free to add new metrics or dimensions as needed per your requirements.
        Set<String> metrics = new HashSet<>(HexMetrcsFilterConstants.HEX_MINIMAL_METRIC_NAMES_FILTER);
        metrics.addAll(METRIC_NAMES);

        return ReporterFilter.compose(
                new MetricNameWhitelistFilter(Collections.unmodifiableSet(metrics)),
                new PropertyWhitelistFilter(Collections.unmodifiableSet(dimensions)));
    }
}
