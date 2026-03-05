package com.penpot.ai.application.service;

import com.penpot.ai.model.MarketingTemplate;
import org.junit.jupiter.api.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("RagTemplateService — Integration")
class RagTemplateServiceTest {

    @MockitoBean
    private VectorStore vectorStore;

    @MockitoBean
    private EmbeddingCacheService embeddingCacheService;

    @Autowired
    private RagTemplateService ragTemplateService;

    @Nested
    @DisplayName("init (@PostConstruct)")
    class InitTests {

        @Test
        @DisplayName("init — service is loaded in Spring context without throwing")
        void init_serviceIsLoadedInSpringContextWithoutThrowing() {
            // GIVEN / WHEN

            // THEN
            assertThat(ragTemplateService).isNotNull();
        }

        @Test
        @DisplayName("init — template count is greater than 0 after loading test JSON files")
        void init_templateCountIsGreaterThanZeroAfterLoadingTestJsonFiles() {
            // GIVEN / WHEN

            // THEN
            assertThat(ragTemplateService.getTemplateCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("init — all available types are non-null and non-blank")
        void init_allAvailableTypesAreNonNullAndNonBlank() {
            // GIVEN / WHEN
            Set<String> types = ragTemplateService.getAvailableTypes();

            // THEN
            assertThat(types).isNotEmpty();
            assertThat(types).allSatisfy(type ->
                assertThat(type).isNotNull().isNotBlank()
            );
        }

        @Test
        @DisplayName("init — all loaded templates have a non-null non-blank id")
        void init_allLoadedTemplatesHaveNonNullNonBlankId() {
            // GIVEN / WHEN
            List<MarketingTemplate> all = ragTemplateService.getAllTemplates();

            // THEN
            assertThat(all).isNotEmpty();
            assertThat(all).allSatisfy(t ->
                assertThat(t.getId()).isNotNull().isNotBlank()
            );
        }
    }

    @Nested
    @DisplayName("searchTemplates")
    class SearchTemplatesIntegrationTests {

        @Test
        @DisplayName("searchTemplates — calls embeddingCacheService.embedQuery with the provided query")
        void searchTemplates_callsEmbeddingCacheServiceEmbedQueryWithProvidedQuery() {
            // GIVEN
            when(embeddingCacheService.embedQuery(anyString())).thenReturn(new float[]{0.1f, 0.2f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            // WHEN
            ragTemplateService.searchTemplates("instagram post");

            // THEN
            verify(embeddingCacheService).embedQuery("instagram post");
        }

        @Test
        @DisplayName("searchTemplates — calls vectorStore.similaritySearch after computing embedding")
        void searchTemplates_callsVectorStoreSimilaritySearchAfterComputingEmbedding() {
            // GIVEN
            when(embeddingCacheService.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            // WHEN
            ragTemplateService.searchTemplates("product launch");

            // THEN
            verify(vectorStore).similaritySearch(any(SearchRequest.class));
        }

        @Test
        @DisplayName("searchTemplates — returns empty list when vectorStore returns no documents")
        void searchTemplates_returnsEmptyListWhenVectorStoreReturnsNoDocuments() {
            // GIVEN
            when(embeddingCacheService.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            // WHEN
            List<MarketingTemplate> result = ragTemplateService.searchTemplates("no match");

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("searchTemplates — returns matching template when vectorStore document id matches a loaded template")
        void searchTemplates_returnsMatchingTemplateWhenVectorStoreDocumentIdMatchesLoadedTemplate() {
            // GIVEN
            List<MarketingTemplate> all = ragTemplateService.getAllTemplates();
            assertThat(all).isNotEmpty();
            String existingId = all.get(0).getId();

            Map<String, Object> meta = new HashMap<>();
            meta.put("id", existingId);
            Document doc = new Document("content", meta);

            when(embeddingCacheService.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

            // WHEN
            List<MarketingTemplate> result = ragTemplateService.searchTemplates("social media");

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(existingId);
        }

        @Test
        @DisplayName("searchTemplates — filters out document whose id is absent from the loaded templates cache")
        void searchTemplates_filtersOutDocumentWhoseIdIsAbsentFromLoadedTemplatesCache() {
            // GIVEN
            Map<String, Object> meta = new HashMap<>();
            meta.put("id", "ghost-template-id-99999");
            Document ghostDoc = new Document("content", meta);

            when(embeddingCacheService.embedQuery(anyString())).thenReturn(new float[]{0.1f});
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(ghostDoc));

            // WHEN
            List<MarketingTemplate> result = ragTemplateService.searchTemplates("query");

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplatesByType")
    class GetTemplatesByTypeIntegrationTests {

        @Test
        @DisplayName("getTemplatesByType — returns non-empty list for a type present in loaded test JSON files")
        void getTemplatesByType_returnsNonEmptyListForTypePresentInLoadedTestJsonFiles() {
            // GIVEN
            Set<String> types = ragTemplateService.getAvailableTypes();
            assertThat(types).isNotEmpty();
            String firstType = types.iterator().next();

            // WHEN
            List<MarketingTemplate> result = ragTemplateService.getTemplatesByType(firstType);

            // THEN
            assertThat(result).isNotEmpty();
            assertThat(result).allSatisfy(t ->
                assertThat(t.getType()).isEqualToIgnoringCase(firstType)
            );
        }

        @Test
        @DisplayName("getTemplatesByType — returns empty list for a type not present in any loaded file")
        void getTemplatesByType_returnsEmptyListForTypeNotPresentInAnyLoadedFile() {
            // GIVEN / WHEN
            List<MarketingTemplate> result = ragTemplateService.getTemplatesByType("nonexistent_type_xyz");

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplatesByTag")
    class GetTemplatesByTagIntegrationTests {

        @Test
        @DisplayName("getTemplatesByTag — returns non-empty list for a tag present in loaded test JSON files")
        void getTemplatesByTag_returnsNonEmptyListForTagPresentInLoadedTestJsonFiles() {
            // GIVEN
            Set<String> availableTags = ragTemplateService.getAvailableTags();
            assertThat(availableTags).isNotEmpty();
            String firstTag = availableTags.iterator().next();

            // WHEN
            List<MarketingTemplate> result = ragTemplateService.getTemplatesByTag(firstTag);

            // THEN
            assertThat(result).isNotEmpty();
            assertThat(result).allSatisfy(t ->
                assertThat(t.getTags()).contains(firstTag)
            );
        }

        @Test
        @DisplayName("getTemplatesByTag — returns empty list for a tag absent from all loaded templates")
        void getTemplatesByTag_returnsEmptyListForTagAbsentFromAllLoadedTemplates() {
            // GIVEN / WHEN
            List<MarketingTemplate> result = ragTemplateService.getTemplatesByTag("nonexistent-tag-xyz-999");

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTemplateById")
    class GetTemplateByIdIntegrationTests {

        @Test
        @DisplayName("getTemplateById — returns present Optional for a real template id from loaded JSON files")
        void getTemplateById_returnsPresentOptionalForRealTemplateId() {
            // GIVEN — pick a real id from loaded templates
            List<MarketingTemplate> all = ragTemplateService.getAllTemplates();
            assertThat(all).isNotEmpty();
            String realId = all.get(0).getId();

            // WHEN
            Optional<MarketingTemplate> result = ragTemplateService.getTemplateById(realId);

            // THEN
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(realId);
        }

        @Test
        @DisplayName("getTemplateById — returns Optional.empty for an id that does not exist")
        void getTemplateById_returnsEmptyOptionalForNonExistentId() {
            // GIVEN / WHEN
            Optional<MarketingTemplate> result = ragTemplateService.getTemplateById("does-not-exist");

            // THEN
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAllTemplates")
    class GetAllTemplatesIntegrationTests {

        @Test
        @DisplayName("getAllTemplates — returns non-empty list after @PostConstruct load")
        void getAllTemplates_returnsNonEmptyListAfterPostConstructLoad() {
            // GIVEN / WHEN
            List<MarketingTemplate> all = ragTemplateService.getAllTemplates();

            // THEN
            assertThat(all).isNotEmpty();
        }

        @Test
        @DisplayName("getAllTemplates — size matches getTemplateCount()")
        void getAllTemplates_sizeMatchesGetTemplateCount() {
            // GIVEN / WHEN
            List<MarketingTemplate> all = ragTemplateService.getAllTemplates();
            int count = ragTemplateService.getTemplateCount();

            // THEN
            assertThat(all).hasSize(count);
        }

        @Test
        @DisplayName("getAllTemplates — returns a new list instance on each call (defensive copy)")
        void getAllTemplates_returnsNewListInstanceOnEachCall() {
            // GIVEN / WHEN
            List<MarketingTemplate> first  = ragTemplateService.getAllTemplates();
            List<MarketingTemplate> second = ragTemplateService.getAllTemplates();

            // THEN
            assertThat(first).isNotSameAs(second);
        }
    }
}