package com.amazon.green.book.service.webapp.utils;

import com.amazon.metrics.declarative.MetricsManager;
import javax.measure.unit.Unit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MetricsEmitter {

    private final MetricsManager metricsManager;

    /**
     * Emit a count metric.
     *
     * @param metricName metric name
     * @param value      count value
     */
    public void emitCount(final String metricName, final double value) {
        metricsManager.get().addCount(metricName, value, Unit.ONE);
    }
}
