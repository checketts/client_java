package io.prometheus.client.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.Summary;
import io.prometheus.client.SummaryMetricFamily;

import java.util.ArrayList;
import java.util.List;


/**
 * Collect metrics from Caffiene's com.github.benmanes.caffeine.cache.Cache.
 * <p>
 * <pre>{@code
 *
 * // Note that `recordStats()` is required to gather non-zero statistics
 * Cache<String, String> cache = Caffeine.newBuilder().recordStats().build();
 * new CacheMetricsCollector(cache, "myapp_mycache").register();
 *
 * }</pre>
 *
 * Exposed metrics are prefixed with the provided prefix.
 *
 * With the example above sample metric names would be:
 * <pre>
 *     myapp_mycache_cache_hit_total 10.0
 *     myapp_mycache_cache_miss_total 3.0
 *     myapp_mycache_cache_eviction_total 1.0
 * </pre>
 *
 * Additionally if the cache includes a loader additional metrics would be provided:
 * <pre>
 *     myapp_mycache_cache_load_success_total 5.0
 *     myapp_mycache_cache_load_error_total 1.0
 *     myapp_mycache_cache_load_sum_seconds 0.0034
 * </pre>
 *
 */
public class CacheMetricsCollector extends Collector {

    private final Cache cache;
    private final String cacheName;

    public CacheMetricsCollector(Cache cache, String cacheName) {
        this.cache = cache;
        this.cacheName = cacheName;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        CacheStats stats = cache.stats();

        mfs.add(new CounterMetricFamily(cacheName + "_cache_hit_total",
                cacheName + " cache hit totals", stats.hitCount()));
        mfs.add(new CounterMetricFamily(cacheName + "_cache_miss_total",
                cacheName + " cache miss totals", stats.missCount()));
        mfs.add(new CounterMetricFamily(cacheName + "_cache_requests_total",
                cacheName + " cache miss totals", stats.requestCount()));
        mfs.add(new CounterMetricFamily(cacheName + "_cache_eviction_total",
                cacheName + " cache eviction totals", stats.evictionCount()));

        if(cache instanceof LoadingCache) {
            mfs.add(new CounterMetricFamily(cacheName+"_cache_load_success_total",
                    cacheName + " cache load success totals", stats.loadSuccessCount()));
            mfs.add(new CounterMetricFamily(cacheName+"_cache_load_failure_total",
                    cacheName + " cache load failure totals", stats.loadFailureCount()));

            mfs.add(new SummaryMetricFamily(cacheName+"_cache_load_duration_seconds",
                    cacheName + " cache load total time in seconds", stats.loadCount(),
                    stats.totalLoadTime() / Collector.NANOSECONDS_PER_SECOND));
        }

        return mfs;
    }
}
