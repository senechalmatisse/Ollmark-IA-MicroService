package com.penpot.ai.application.service;

import com.penpot.ai.model.MarketingTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.Strictness;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RagTemplateService — Unit")
public class RagTemplateServiceUnit {

    @Mock private VectorStore            vectorStore;
    @Mock private ObjectMapper           objectMapper;
    @Mock private EmbeddingCacheService  embeddingCache;

    @InjectMocks
    private RagTemplateService service;

    private MarketingTemplate makeTemplate(String id, String type, List<String> tags) {
        MarketingTemplate t = mock(MarketingTemplate.class);
        when(t.getId()).thenReturn(id);
        when(t.getType()).thenReturn(type);
        when(t.getTags()).thenReturn(tags);
        return t;
    }

    private void injectCache(Map<String, MarketingTemplate> cache) {
        ReflectionTestUtils.setField(service, "templatesCache", cache);
    }

    private Document docWithId(String templateId) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("id", templateId);
        return new Document("content " + templateId, meta);
    }

    @Nested
    @DisplayName("searchTemplates")
    class SearchTemplatesTests {

        @Test
        @DisplayName("searchTemplates — calls embeddingCache.embedQuery with the provided query")
        void searchTemplates_callsEmbeddingCacheEmbedQueryWithProvidedQuery() {
            // GIVEN
            when(embeddingCache.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            // WHEN
            service.searchTemplates("instagram post");

            // THEN
            verify(embeddingCache).embedQuery("instagram post");
        }

        @Test
        @DisplayName("searchTemplates — calls vectorStore.similaritySearch after computing embedding")
        void searchTemplates_callsVectorStoreSimilaritySearchAfterComputingEmbedding() {
            // GIVEN
            when(embeddingCache.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            // WHEN
            service.searchTemplates("any query");

            // THEN
            verify(vectorStore).similaritySearch(any(SearchRequest.class));
        }

        @Test
        @DisplayName("searchTemplates — returns empty list when vectorStore returns no results")
        void searchTemplates_returnsEmptyListWhenVectorStoreReturnsNoResults() {
            // GIVEN
            when(embeddingCache.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            // WHEN
            List<MarketingTemplate> result = service.searchTemplates("no match");

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("searchTemplates — returns matching template when vectorStore result id is in cache")
        void searchTemplates_returnsMatchingTemplateWhenVectorStoreResultIdIsInCache() {
            // GIVEN
            MarketingTemplate template = makeTemplate("tmpl-001", "social_media_post", List.of());
            injectCache(Map.of("tmpl-001", template));
            when(embeddingCache.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(docWithId("tmpl-001")));

            // WHEN
            List<MarketingTemplate> result = service.searchTemplates("social media post");

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isSameAs(template);
        }

        @Test
        @DisplayName("searchTemplates — filters out result whose templateId is not in templatesCache")
        void searchTemplates_filtersOutResultWhoseTemplateIdIsNotInTemplatesCache() {
            // GIVEN
            injectCache(Map.of("tmpl-known", makeTemplate("tmpl-known", "email", List.of())));
            when(embeddingCache.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(docWithId("tmpl-unknown")));

            // WHEN
            List<MarketingTemplate> result = service.searchTemplates("query");

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("searchTemplates — returns multiple templates when vectorStore returns multiple matching documents")
        void searchTemplates_returnsMultipleTemplatesWhenVectorStoreReturnsMultipleMatchingDocuments() {
            // GIVEN
            MarketingTemplate t1 = makeTemplate("tmpl-1", "social_media_post", List.of());
            MarketingTemplate t2 = makeTemplate("tmpl-2", "email", List.of());
            injectCache(Map.of("tmpl-1", t1, "tmpl-2", t2));
            when(embeddingCache.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(docWithId("tmpl-1"), docWithId("tmpl-2")));

            // WHEN
            List<MarketingTemplate> result = service.searchTemplates("post");

            // THEN
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(t1, t2);
        }

        @Test
        @DisplayName("searchTemplates — returns only templates with valid cache entries when mix of known and unknown ids")
        void searchTemplates_returnsOnlyTemplatesWithValidCacheEntriesForMixedIds() {
            // GIVEN
            MarketingTemplate known = makeTemplate("tmpl-known", "social_media_post", List.of());
            injectCache(Map.of("tmpl-known", known));
            when(embeddingCache.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(docWithId("tmpl-known"), docWithId("tmpl-ghost")));

            // WHEN
            List<MarketingTemplate> result = service.searchTemplates("post");

            // THEN
            assertThat(result).hasSize(1).containsExactly(known);
        }
    }

    @Nested
    @DisplayName("getTemplatesByType")
    class GetTemplatesByTypeTests {

        @Test
        @DisplayName("getTemplatesByType — returns empty list when cache is empty")
        void getTemplatesByType_returnsEmptyListWhenCacheIsEmpty() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getTemplatesByType("email")).isEmpty();
        }

        @Test
        @DisplayName("getTemplatesByType — returns only templates matching the given type")
        void getTemplatesByType_returnsOnlyTemplatesMatchingGivenType() {
            // GIVEN
            MarketingTemplate email1 = makeTemplate("e-1", "email", List.of());
            MarketingTemplate email2 = makeTemplate("e-2", "email", List.of());
            MarketingTemplate social = makeTemplate("s-1", "social_media_post", List.of());
            injectCache(Map.of("e-1", email1, "e-2", email2, "s-1", social));

            // WHEN
            List<MarketingTemplate> result = service.getTemplatesByType("email");

            // THEN
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(email1, email2);
        }

        @Test
        @DisplayName("getTemplatesByType — is case-insensitive when matching type")
        void getTemplatesByType_isCaseInsensitiveWhenMatchingType() {
            // GIVEN
            MarketingTemplate t = makeTemplate("t-1", "Email", List.of());
            injectCache(Map.of("t-1", t));

            // WHEN / THEN
            assertThat(service.getTemplatesByType("email")).containsExactly(t);
        }

        @Test
        @DisplayName("getTemplatesByType — returns empty list when no template matches the given type")
        void getTemplatesByType_returnsEmptyListWhenNoTemplateMatchesGivenType() {
            // GIVEN
            injectCache(Map.of("t-1", makeTemplate("t-1", "social_media_post", List.of())));

            // WHEN / THEN
            assertThat(service.getTemplatesByType("poster_a3")).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplatesByTag")
    class GetTemplatesByTagTests {

        @Test
        @DisplayName("getTemplatesByTag — returns empty list when cache is empty")
        void getTemplatesByTag_returnsEmptyListWhenCacheIsEmpty() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getTemplatesByTag("instagram")).isEmpty();
        }

        @Test
        @DisplayName("getTemplatesByTag — returns only templates that have the given tag")
        void getTemplatesByTag_returnsOnlyTemplatesThatHaveGivenTag() {
            // GIVEN
            MarketingTemplate withTag    = makeTemplate("t-1", "social_media_post", List.of("instagram", "product"));
            MarketingTemplate withoutTag = makeTemplate("t-2", "email", List.of("newsletter"));
            injectCache(Map.of("t-1", withTag, "t-2", withoutTag));

            // WHEN / THEN
            assertThat(service.getTemplatesByTag("instagram")).containsExactly(withTag);
        }

        @Test
        @DisplayName("getTemplatesByTag — skips templates whose tags list is null")
        void getTemplatesByTag_skipsTemplatesWhoseTagsListIsNull() {
            // GIVEN
            injectCache(Map.of("t-null", makeTemplate("t-null", "email", null)));

            // WHEN / THEN
            assertThat(service.getTemplatesByTag("any-tag")).isEmpty();
        }

        @Test
        @DisplayName("getTemplatesByTag — returns empty list when no template has the given tag")
        void getTemplatesByTag_returnsEmptyListWhenNoTemplateHasGivenTag() {
            // GIVEN
            injectCache(Map.of("t-1", makeTemplate("t-1", "email", List.of("newsletter"))));

            // WHEN / THEN
            assertThat(service.getTemplatesByTag("instagram")).isEmpty();
        }

        @Test
        @DisplayName("getTemplatesByTag — returns multiple templates when several have the given tag")
        void getTemplatesByTag_returnsMultipleTemplatesWhenSeveralHaveGivenTag() {
            // GIVEN
            MarketingTemplate t1 = makeTemplate("t-1", "social_media_post", List.of("brand", "summer"));
            MarketingTemplate t2 = makeTemplate("t-2", "poster_a3", List.of("brand", "event"));
            MarketingTemplate t3 = makeTemplate("t-3", "email", List.of("newsletter"));
            injectCache(Map.of("t-1", t1, "t-2", t2, "t-3", t3));

            // WHEN
            List<MarketingTemplate> result = service.getTemplatesByTag("brand");

            // THEN
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(t1, t2);
        }
    }

    @Nested
    @DisplayName("getTemplateById")
    class GetTemplateByIdTests {

        @Test
        @DisplayName("getTemplateById — returns Optional containing the template when id exists in cache")
        void getTemplateById_returnsOptionalContainingTemplateWhenIdExistsInCache() {
            // GIVEN
            MarketingTemplate template = makeTemplate("tmpl-abc", "email", List.of());
            injectCache(Map.of("tmpl-abc", template));

            // WHEN
            Optional<MarketingTemplate> result = service.getTemplateById("tmpl-abc");

            // THEN
            assertThat(result).isPresent().contains(template);
        }

        @Test
        @DisplayName("getTemplateById — returns Optional.empty when id is not in cache")
        void getTemplateById_returnsEmptyOptionalWhenIdIsNotInCache() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getTemplateById("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("getTemplateById — returns Optional.empty when id is null")
        void getTemplateById_returnsEmptyOptionalWhenIdIsNull() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getTemplateById(null)).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllTemplates")
    class GetAllTemplatesTests {

        @Test
        @DisplayName("getAllTemplates — returns empty list when cache is empty")
        void getAllTemplates_returnsEmptyListWhenCacheIsEmpty() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getAllTemplates()).isEmpty();
        }

        @Test
        @DisplayName("getAllTemplates — returns all templates from cache")
        void getAllTemplates_returnsAllTemplatesFromCache() {
            // GIVEN
            MarketingTemplate t1 = makeTemplate("t-1", "email", List.of());
            MarketingTemplate t2 = makeTemplate("t-2", "social_media_post", List.of());
            injectCache(Map.of("t-1", t1, "t-2", t2));

            // WHEN
            List<MarketingTemplate> result = service.getAllTemplates();

            // THEN
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(t1, t2);
        }

        @Test
        @DisplayName("getAllTemplates — returns a new list instance on each call (defensive copy via new ArrayList)")
        void getAllTemplates_returnsNewListInstanceOnEachCall() {
            // GIVEN
            injectCache(Map.of("t-1", makeTemplate("t-1", "email", List.of())));

            // WHEN
            List<MarketingTemplate> first  = service.getAllTemplates();
            List<MarketingTemplate> second = service.getAllTemplates();

            // THEN
            assertThat(first).isNotSameAs(second);
        }
    }

    @Nested
    @DisplayName("getTemplateCount")
    class GetTemplateCountTests {

        @Test
        @DisplayName("getTemplateCount — returns 0 when cache is empty")
        void getTemplateCount_returnsZeroWhenCacheIsEmpty() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getTemplateCount()).isZero();
        }

        @Test
        @DisplayName("getTemplateCount — returns 1 when cache contains one template")
        void getTemplateCount_returnsOneWhenCacheContainsOneTemplate() {
            // GIVEN
            injectCache(Map.of("t-1", makeTemplate("t-1", "email", List.of())));

            // WHEN / THEN
            assertThat(service.getTemplateCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("getTemplateCount — returns 3 when cache contains three templates")
        void getTemplateCount_returnsThreeWhenCacheContainsThreeTemplates() {
            // GIVEN
            injectCache(Map.of(
                "t-1", makeTemplate("t-1", "email", List.of()),
                "t-2", makeTemplate("t-2", "social_media_post", List.of()),
                "t-3", makeTemplate("t-3", "poster_a3", List.of())
            ));

            // WHEN / THEN
            assertThat(service.getTemplateCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getAvailableTypes")
    class GetAvailableTypesTests {

        @Test
        @DisplayName("getAvailableTypes — returns empty set when cache is empty")
        void getAvailableTypes_returnsEmptySetWhenCacheIsEmpty() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getAvailableTypes()).isEmpty();
        }

        @Test
        @DisplayName("getAvailableTypes — returns a set of distinct types from all templates")
        void getAvailableTypes_returnsDistinctTypesFromAllTemplates() {
            // GIVEN
            injectCache(Map.of(
                "t-1", makeTemplate("t-1", "email", List.of()),
                "t-2", makeTemplate("t-2", "email", List.of()),
                "t-3", makeTemplate("t-3", "social_media_post", List.of())
            ));

            // WHEN
            Set<String> result = service.getAvailableTypes();

            // THEN
            assertThat(result).containsExactlyInAnyOrder("email", "social_media_post");
        }

        @Test
        @DisplayName("getAvailableTypes — returns a single type when all templates have the same type")
        void getAvailableTypes_returnsSingleTypeWhenAllTemplatesHaveSameType() {
            // GIVEN
            injectCache(Map.of(
                "t-1", makeTemplate("t-1", "poster_a3", List.of()),
                "t-2", makeTemplate("t-2", "poster_a3", List.of())
            ));

            // WHEN / THEN
            assertThat(service.getAvailableTypes()).containsExactly("poster_a3");
        }
    }

    @Nested
    @DisplayName("getAvailableTags")
    class GetAvailableTagsTests {

        @Test
        @DisplayName("getAvailableTags — returns empty set when cache is empty")
        void getAvailableTags_returnsEmptySetWhenCacheIsEmpty() {
            // GIVEN
            injectCache(new HashMap<>());

            // WHEN / THEN
            assertThat(service.getAvailableTags()).isEmpty();
        }

        @Test
        @DisplayName("getAvailableTags — returns empty set when all templates have null tags")
        void getAvailableTags_returnsEmptySetWhenAllTemplatesHaveNullTags() {
            // GIVEN
            injectCache(Map.of(
                "t-1", makeTemplate("t-1", "email", null),
                "t-2", makeTemplate("t-2", "social_media_post", null)
            ));

            // WHEN / THEN
            assertThat(service.getAvailableTags()).isEmpty();
        }

        @Test
        @DisplayName("getAvailableTags — returns union of all tags with deduplication when templates share tags")
        void getAvailableTags_returnsUnionOfAllTagsWithDeduplicationWhenTemplatesShareTags() {
            // GIVEN
            injectCache(Map.of(
                "t-1", makeTemplate("t-1", "email",             List.of("brand", "newsletter")),
                "t-2", makeTemplate("t-2", "social_media_post", List.of("brand", "instagram"))
            ));

            // WHEN
            Set<String> result = service.getAvailableTags();

            // THEN
            assertThat(result).containsExactlyInAnyOrder("brand", "newsletter", "instagram");
        }

        @Test
        @DisplayName("getAvailableTags — skips templates with null tags and includes tags from non-null ones")
        void getAvailableTags_skipsNullTagsTemplatesAndIncludesTagsFromNonNullOnes() {
            // GIVEN
            injectCache(Map.of(
                "t-null", makeTemplate("t-null", "email", null),
                "t-tags", makeTemplate("t-tags", "social_media_post", List.of("summer", "event"))
            ));

            // WHEN
            Set<String> result = service.getAvailableTags();

            // THEN
            assertThat(result).containsExactlyInAnyOrder("summer", "event");
        }
    }
}