package com.penpot.ai.infrastructure.config;

import com.github.benmanes.caffeine.cache.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmbeddingCacheConfig")
class EmbeddingCacheConfigTest {

    @Nested
    @DisplayName("Cache name constants — Unit")
    class CacheNameConstantTests {

        @Test
        @DisplayName("QUERY_EMBEDDINGS_CACHE — valeur = 'query-embeddings'")
        void queryEmbeddingsCache_constantValueIsCorrect() {
            assertThat(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE)
                .isEqualTo("query-embeddings");
        }

        @Test
        @DisplayName("DOCUMENT_EMBEDDINGS_CACHE — valeur = 'document-embeddings'")
        void documentEmbeddingsCache_constantValueIsCorrect() {
            assertThat(EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE)
                .isEqualTo("document-embeddings");
        }
    }

    @Nested
    @DisplayName("cacheManager bean — Spring Integration")
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = EmbeddingCacheConfig.class)
    class CacheManagerBeanTests {

        @Autowired
        private CacheManager cacheManager;

        @Test
        @DisplayName("bean chargé et non-null")
        void cacheManager_beanIsLoadedInSpringContextAndIsNotNull() {
            assertThat(cacheManager).isNotNull();
        }

        @Test
        @DisplayName("implémentation = CaffeineCacheManager")
        void cacheManager_isACaffeineCacheManagerInstance() {
            assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        }

        @Test
        @DisplayName("contient le cache query-embeddings")
        void cacheManager_containsQueryEmbeddingsCache() {
            Collection<String> names = cacheManager.getCacheNames();
            assertThat(names).contains(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
        }

        @Test
        @DisplayName("contient le cache document-embeddings")
        void cacheManager_containsDocumentEmbeddingsCache() {
            Collection<String> names = cacheManager.getCacheNames();
            assertThat(names).contains(EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE);
        }

        @Test
        @DisplayName("contient exactement les deux caches configurés")
        void cacheManager_containsExactlyTwoConfiguredCacheNames() {
            Collection<String> names = cacheManager.getCacheNames();
            assertThat(names).containsExactlyInAnyOrder(
                EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE,
                EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE
            );
        }
    }

    @Nested
    @DisplayName("Cache behavior — Spring Integration")
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = EmbeddingCacheConfig.class)
    class CacheBehaviorTests {

        @Autowired
        private CacheManager cacheManager;

        @Test
        @DisplayName("query-embeddings — stocke et récupère une valeur")
        void queryEmbeddingsCache_canStoreAndRetrieveValue() {
            org.springframework.cache.Cache cache =
                    cacheManager.getCache(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();

            cache.put("test-key", "test-value");
            ValueWrapper result = cache.get("test-key");

            assertThat(result).isNotNull();
            assertThat(result.get()).isEqualTo("test-value");
        }

        @Test
        @DisplayName("document-embeddings — stocke et récupère une valeur")
        void documentEmbeddingsCache_canStoreAndRetrieveValue() {
            org.springframework.cache.Cache cache =
                    cacheManager.getCache(EmbeddingCacheConfig.DOCUMENT_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();

            cache.put("doc-key", "doc-value");
            ValueWrapper result = cache.get("doc-key");

            assertThat(result).isNotNull();
            assertThat(result.get()).isEqualTo("doc-value");
        }

        @Test
        @DisplayName("query-embeddings — retourne null pour une clé absente")
        void queryEmbeddingsCache_returnsNullForMissingKey() {
            org.springframework.cache.Cache cache =
                    cacheManager.getCache(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();

            ValueWrapper result = cache.get("non-existent-key-" + System.nanoTime());

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("query-embeddings — évince une valeur précédemment stockée")
        void queryEmbeddingsCache_evictsPreviouslyStoredValue() {
            org.springframework.cache.Cache cache =
                    cacheManager.getCache(EmbeddingCacheConfig.QUERY_EMBEDDINGS_CACHE);
            assertThat(cache).isNotNull();

            cache.put("evict-key", "evict-value");
            cache.evict("evict-key");

            assertThat(cache.get("evict-key")).isNull();
        }
    }

    @Nested
    @DisplayName("Eviction listener — Unit")
    class EvictionListenerTests {

        @Test
        @DisplayName("invoqué avec la clé et la cause quand la capacité est dépassée")
        void evictionListener_isInvokedWithKeyAndCauseWhenCapacityExceeded() {
            AtomicBoolean listenerCalled = new AtomicBoolean(false);
            AtomicReference<Object> evictedKey = new AtomicReference<>();
            AtomicReference<RemovalCause> evictedCause  = new AtomicReference<>();

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

            testCache.put("key-to-evict",  "value-1");
            testCache.put("key-that-stays", "value-2");
            testCache.cleanUp();

            assertThat(listenerCalled.get())
                .as("le listener doit être appelé après dépassement de capacité")
                .isTrue();
            assertThat(evictedKey.get())
                .as("la clé évincée doit être la première insérée")
                .isEqualTo("key-to-evict");
            assertThat(evictedCause.get())
                .as("la cause doit être SIZE (capacité dépassée)")
                .isEqualTo(RemovalCause.SIZE);
        }

        @Test
        @DisplayName("non invoqué quand aucune entrée n'est ajoutée ou supprimée")
        void evictionListener_isNotInvokedWhenNoEntryIsAddedOrRemoved() {
            AtomicBoolean listenerCalled = new AtomicBoolean(false);

            Cache<Object, Object> testCache = Caffeine.newBuilder()
                .maximumSize(100)
                .recordStats()
                .weakKeys()
                .evictionListener((key, value, cause) -> listenerCalled.set(true))
                .build();

            testCache.cleanUp();

            assertThat(listenerCalled.get())
                .as("le listener ne doit pas être appelé sans ajout ni suppression")
                .isFalse();
        }
    }
}