package com.penpot.ai.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.*;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.cache.Cache.*;

import java.util.Collection;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("EmbeddingCacheConfig — Integration")
public class EmbeddingCacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Nested
    @DisplayName("Cache name constants")
    class CacheNameConstantTests {

        @Test
        @DisplayName("QUERY_EMBEDDINGS_CACHE — constant value is 'query-embeddings'")
        void queryEmbeddingsCache_constantValueIsCorrect() {
            // GIVEN / WHEN / THEN
            assertThat(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE).isEqualTo("query-embeddings");
        }

        @Test
        @DisplayName("DOCUMENT_EMBEDDINGS_CACHE — constant value is 'document-embeddings'")
        void documentEmbeddingsCache_constantValueIsCorrect() {
            // GIVEN / WHEN / THEN
            assertThat(EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE).isEqualTo("document-embeddings");
        }
    }

    @Nested
    @DisplayName("cacheManager bean")
    class CacheManagerBeanTests {

        @Test
        @DisplayName("cacheManager — bean is loaded in Spring context and is not null")
        void cacheManager_beanIsLoadedInSpringContextAndIsNotNull() {
            // GIVEN / WHEN / THEN
            assertThat(cacheManager).isNotNull();
        }

        @Test
        @DisplayName("cacheManager — is a CaffeineCacheManager instance")
        void cacheManager_isACaffeineCacheManagerInstance() {
            // GIVEN / WHEN / THEN
            assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        }

        @Test
        @DisplayName("cacheManager — contains the query-embeddings cache")
        void cacheManager_containsQueryEmbeddingsCache() {
            // GIVEN / WHEN
            Collection<String> cacheNames = cacheManager.getCacheNames();

            // THEN
            assertThat(cacheNames).contains(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
        }

        @Test
        @DisplayName("cacheManager — contains the document-embeddings cache")
        void cacheManager_containsDocumentEmbeddingsCache() {
            // GIVEN / WHEN
            Collection<String> cacheNames = cacheManager.getCacheNames();

            // THEN
            assertThat(cacheNames).contains(EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE);
        }

        @Test
        @DisplayName("cacheManager — contains exactly the two configured cache names")
        void cacheManager_containsExactlyTwoConfiguredCacheNames() {
            // GIVEN / WHEN
            Collection<String> cacheNames = cacheManager.getCacheNames();

            // THEN
            assertThat(cacheNames).containsExactlyInAnyOrder(
                EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE,
                EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE
            );
        }
    }

    @Nested
    @DisplayName("Cache behavior")
    class CacheBehaviorTests {

        @Test
        @DisplayName("query-embeddings cache — can store and retrieve a value")
        void queryEmbeddingsCache_canStoreAndRetrieveValue() {
            // GIVEN
            org.springframework.cache.Cache cache =
                cacheManager.getCache(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();

            // WHEN
            cache.put("test-key", "test-value");
            ValueWrapper result = cache.get("test-key");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.get()).isEqualTo("test-value");
        }

        @Test
        @DisplayName("document-embeddings cache — can store and retrieve a value")
        void documentEmbeddingsCache_canStoreAndRetrieveValue() {
            // GIVEN
            org.springframework.cache.Cache cache =
                cacheManager.getCache(EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();

            // WHEN
            cache.put("doc-key", "doc-value");
            ValueWrapper result = cache.get("doc-key");

            // THEN
            assertThat(result).isNotNull();
            assertThat(result.get()).isEqualTo("doc-value");
        }

        @Test
        @DisplayName("query-embeddings cache — returns null for a missing key")
        void queryEmbeddingsCache_returnsNullForMissingKey() {
            // GIVEN
            org.springframework.cache.Cache cache =
                cacheManager.getCache(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();

            // WHEN
            ValueWrapper result =
                cache.get("non-existent-key-" + System.nanoTime());

            // THEN
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("query-embeddings cache — evicts a previously stored value")
        void queryEmbeddingsCache_evictsPreviouslyStoredValue() {
            // GIVEN
            org.springframework.cache.Cache cache =
                cacheManager.getCache(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();
            cache.put("evict-key", "evict-value");

            // WHEN
            cache.evict("evict-key");

            // THEN
            assertThat(cache.get("evict-key")).isNull();
        }
    }

    @Nested
    @DisplayName("Eviction listener")
    class EvictionListenerTests {

        @Test
        @DisplayName("evictionListener — is invoked with the evicted key and cause when cache capacity is exceeded")
        void evictionListener_isInvokedWithKeyAndCauseWhenCapacityExceeded() throws InterruptedException {
            // GIVEN
            AtomicBoolean listenerCalled = new AtomicBoolean(false);
            AtomicReference<Object> evictedKey = new AtomicReference<>();
            AtomicReference<RemovalCause> evictedCause = new AtomicReference<>();

            Cache<Object, Object> testCache = Caffeine.newBuilder()
                .maximumSize(1)
                .recordStats()
                .weakKeys()
                .evictionListener((key, value, cause) -> {
                    listenerCalled.set(true);
                    evictedKey.set(key);
                    evictedCause.set(cause);
                })
                .build();

            // WHEN
            testCache.put("key-to-evict", "value-1");
            testCache.put("key-that-stays", "value-2");
            testCache.cleanUp();

            // THEN
            assertThat(listenerCalled.get())
                .as("evictionListener should have been called after capacity overflow")
                .isTrue();
            assertThat(evictedKey.get())
                .as("evicted key should be the first inserted key")
                .isEqualTo("key-to-evict");
            assertThat(evictedCause.get())
                .as("eviction cause should be SIZE (capacity exceeded)")
                .isEqualTo(RemovalCause.SIZE);
        }

        @Test
        @DisplayName("evictionListener — is NOT invoked when no entry is added or removed")
        void evictionListener_isNotInvokedWhenNoEntryIsAddedOrRemoved() {
            // GIVEN
            AtomicBoolean listenerCalled = new AtomicBoolean(false);

            Cache<Object, Object> testCache = Caffeine.newBuilder()
                .maximumSize(100)
                .recordStats()
                .weakKeys()
                .evictionListener((key, value, cause) -> listenerCalled.set(true))
                .build();

            // WHEN
            testCache.cleanUp();

            // THEN
            assertThat(listenerCalled.get())
                .as("evictionListener should not be called when no entry was added or removed")
                .isFalse();
        }
    }
}