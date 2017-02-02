package io.prometheus.client.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.cache.caffeine.CacheMetricsCollector;
import org.junit.Test;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CacheMetricsCollectorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void cacheExposesMetricsForHitMissAndEviction() throws Exception {
        final AtomicInteger removalsOccured = new AtomicInteger();
        Cache<String, String> cache = Caffeine.newBuilder().maximumSize(2)
                .removalListener(new RemovalListener<Object, Object>() {
                    @Override
                    public void onRemoval(Object key, Object value, RemovalCause cause) {
                        removalsOccured.incrementAndGet();
                    }
                }).recordStats().build();
        CacheMetricsCollector collector = new CacheMetricsCollector(cache, "myapp_users");

        cache.getIfPresent("user1");
        cache.getIfPresent("user1");
        cache.put("user1", "First User");
        cache.getIfPresent("user1");

        // Add to cache to trigger eviction.
        cache.put("user2", "Second User");
        cache.put("user3", "Third User");
        cache.put("user4", "Fourth User");

        CollectorRegistry registry = new CollectorRegistry();
        collector.register(registry);

        assertThat(registry.getSampleValue("myapp_users_cache_hit_total")).isEqualTo(1.0);
        assertThat(registry.getSampleValue("myapp_users_cache_miss_total")).isEqualTo(2.0);

        waitForEvitionToComplete(removalsOccured, 2);
        assertThat(registry.getSampleValue("myapp_users_cache_eviction_total")).isEqualTo(2.0);
    }

    private void waitForEvitionToComplete(AtomicInteger removalsOccured, int desiredRemovals) throws InterruptedException {
        int waits = 0;
        while(removalsOccured.get() < desiredRemovals && waits < 10) {
            Thread.sleep(5);
            waits++;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadingCacheExposesMetricsForLoadsAndExceptions() throws Exception {
        CacheLoader<String, String> loader = mock(CacheLoader.class);
        when(loader.load(anyString()))
                .thenReturn("First User")
                .thenThrow(new RuntimeException("Seconds time fails"))
                .thenReturn("Third User");

        LoadingCache<String, String> cache = Caffeine.newBuilder().recordStats().build(loader);
        CacheMetricsCollector collector = new CacheMetricsCollector(cache, "myapp_loadingusers");

        cache.get("user1");
        cache.get("user1");
        try{
            cache.get("user2");
        } catch (Exception e) {
            // ignoring.
        }
        cache.get("user3");

        CollectorRegistry registry = new CollectorRegistry();
        collector.register(registry);

        assertThat(registry.getSampleValue("myapp_loadingusers_cache_hit_total")).isEqualTo(1.0);
        assertThat(registry.getSampleValue("myapp_loadingusers_cache_miss_total")).isEqualTo(3.0);

        assertThat(registry.getSampleValue("myapp_loadingusers_cache_load_success_total")).isEqualTo(2.0);
        assertThat(registry.getSampleValue("myapp_loadingusers_cache_load_failure_total")).isEqualTo(1.0);

        assertThat(registry.getSampleValue("myapp_loadingusers_cache_load_duration_seconds_count")).isEqualTo(3.0);
        assertThat(registry.getSampleValue("myapp_loadingusers_cache_load_duration_seconds_sum")).isGreaterThan(0.0);
    }


}
