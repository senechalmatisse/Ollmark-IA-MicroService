package com.penpot.ai.application.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmbeddingCacheService — Unit")
public class EmbeddingCacheServiceUnit {

    @Mock private EmbeddingModel embeddingModel;
    @Mock private EmbeddingResponse embeddingResponse;
    @Mock private Embedding embedding;

    @InjectMocks
    private EmbeddingCacheService service;

    private static final float[] VECTOR = {0.1f, 0.2f, 0.3f};

    private void setupEmbeddingModel() {
        when(embeddingModel.embedForResponse(anyList())).thenReturn(embeddingResponse);
        when(embeddingResponse.getResult()).thenReturn(embedding);
        when(embedding.getOutput()).thenReturn(VECTOR);
    }

    @Nested @DisplayName("embedQuery")
    class EmbedQueryTests {

        @Test @DisplayName("embedQuery — calls EmbeddingModel.embedForResponse with query wrapped in a list")
        void embedQuery_callsEmbeddingModelWithQueryInList() {
            // GIVEN
            setupEmbeddingModel();
            String query = "social media post";

            // WHEN
            service.embedQuery(query);

            // THEN
            verify(embeddingModel).embedForResponse(List.of(query));
        }
    }

    @Nested @DisplayName("embedDocument")
    class EmbedDocumentTests {

        @Test @DisplayName("embedDocument — calls EmbeddingModel.embedForResponse with content wrapped in a list")
        void embedDocument_callsEmbeddingModelWithContentInList() {
            // GIVEN
            setupEmbeddingModel();
            String content = "Template ID: tmpl-001\nType: social_media_post";

            // WHEN
            service.embedDocument(content);

            // THEN
            verify(embeddingModel).embedForResponse(List.of(content));
        }
    }
}