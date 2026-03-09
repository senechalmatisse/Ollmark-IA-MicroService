package com.penpot.ai.application.tools;

import com.penpot.ai.application.service.*;
import com.penpot.ai.core.domain.TemplateSpecs;
import com.penpot.ai.infrastructure.strategy.TemplateSpecsFormatter;
import com.penpot.ai.model.MarketingTemplate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateSearchTools — Unit")
public class TemplateSearchToolsUnit {

    @Mock
    private RagTemplateService ragTemplateService;

    @Mock
    private TemplateSpecsExtractor specsExtractor;

    @Mock
    private TemplateSpecsFormatter specsFormatter;

    @InjectMocks
    private TemplateSearchTools templateSearchTools;

    private MarketingTemplate buildTemplate(String id, String type, String description, List<String> tags) {
        MarketingTemplate t = new MarketingTemplate();
        t.setId(id);
        t.setType(type);
        t.setDescription(description);
        t.setTags(tags);
        return t;
    }

    @Nested
    @DisplayName("searchTemplates")
    class SearchTemplatesTests {

        @Test
        @DisplayName("searchTemplates — returns formatted list when templates found")
        void searchTemplates_returnsFormattedList_whenTemplatesFound() {
            // GIVEN
            String query = "modern social media post";
            MarketingTemplate template = buildTemplate("1", "social_media_post", "Modern post",
                    List.of("modern", "minimal"));
            when(ragTemplateService.searchTemplates(query)).thenReturn(List.of(template));

            // WHEN
            String result = templateSearchTools.searchTemplates(query);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("\"count\": 1");
            assertThat(result).contains("social_media_post");
            assertThat(result).contains("getTemplateDesignSpecs");
            verify(ragTemplateService).searchTemplates(query);
        }

        @Test
        @DisplayName("searchTemplates — returns no results with suggestion when no templates found")
        void searchTemplates_returnsNoResults_whenNoTemplatesFound() {
            // GIVEN
            String query = "unknown template xyz";
            when(ragTemplateService.searchTemplates(query)).thenReturn(List.of());

            // WHEN
            String result = templateSearchTools.searchTemplates(query);

            // THEN
            assertThat(result).contains("\"templates\": []");
            assertThat(result).contains("\"count\": 0");
            assertThat(result).contains("listTemplateTypes()");
            verify(ragTemplateService).searchTemplates(query);
        }

        @Test
        @DisplayName("searchTemplates — returns error when query is null")
        void searchTemplates_returnsError_whenQueryIsNull() {
            // GIVEN
            String query = null;

            // WHEN
            String result = templateSearchTools.searchTemplates(query);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Query cannot be empty");
            verify(ragTemplateService, never()).searchTemplates(anyString());
        }

        @Test
        @DisplayName("searchTemplates — returns error when query is blank")
        void searchTemplates_returnsError_whenQueryIsBlank() {
            // GIVEN
            String query = "   ";

            // WHEN
            String result = templateSearchTools.searchTemplates(query);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Query cannot be empty");
            verify(ragTemplateService, never()).searchTemplates(anyString());
        }

        @Test
        @DisplayName("searchTemplates — returns error when service throws exception")
        void searchTemplates_returnsError_whenServiceThrowsException() {
            // GIVEN
            String query = "modern post";
            when(ragTemplateService.searchTemplates(query)).thenThrow(new RuntimeException("Service unavailable"));

            // WHEN
            String result = templateSearchTools.searchTemplates(query);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Search failed");
            verify(ragTemplateService).searchTemplates(query);
        }

        @Test
        @DisplayName("searchTemplates — returns multiple templates when several match")
        void searchTemplates_returnsMultipleTemplates_whenSeveralMatch() {
            // GIVEN
            String query = "promotion flyer";
            List<MarketingTemplate> templates = List.of(
                    buildTemplate("1", "flyer_a5", "Flyer promo 1", List.of("modern")),
                    buildTemplate("2", "flyer_a5", "Flyer promo 2", List.of("classic")),
                    buildTemplate("3", "poster_a4", "Poster promo", List.of("colorful")));
            when(ragTemplateService.searchTemplates(query)).thenReturn(templates);

            // WHEN
            String result = templateSearchTools.searchTemplates(query);

            // THEN
            assertThat(result).contains("\"count\": 3");
            assertThat(result).contains("\"success\": true");
            verify(ragTemplateService).searchTemplates(query);
        }
    }

    // ─── getTemplateDesignSpecs ───────────────────────────────────────────────

    @Nested
    @DisplayName("getTemplateDesignSpecs")
    class GetTemplateDesignSpecsTests {

        @Test
        @DisplayName("getTemplateDesignSpecs — returns formatted specs when template found")
        void getTemplateDesignSpecs_returnsFormattedSpecs_whenTemplateFound() {
            // GIVEN
            String templateId = "template-1";
            MarketingTemplate template = buildTemplate(templateId, "poster_a3", "A3 poster", List.of("modern"));
            TemplateSpecs specs = mock(TemplateSpecs.class);
            String formattedSpecs = "{\"success\": true, \"specs\": {}}";

            when(ragTemplateService.getTemplateById(templateId)).thenReturn(Optional.of(template));
            when(specsExtractor.extractSpecs(template)).thenReturn(specs);
            when(specsFormatter.format(specs)).thenReturn(formattedSpecs);

            // WHEN
            String result = templateSearchTools.getTemplateDesignSpecs(templateId);

            // THEN
            assertThat(result).isEqualTo(formattedSpecs);
            verify(ragTemplateService).getTemplateById(templateId);
            verify(specsExtractor).extractSpecs(template);
            verify(specsFormatter).format(specs);
        }

        @Test
        @DisplayName("getTemplateDesignSpecs — returns error when template not found")
        void getTemplateDesignSpecs_returnsError_whenTemplateNotFound() {
            // GIVEN
            String templateId = "unknown-id";
            when(ragTemplateService.getTemplateById(templateId)).thenReturn(Optional.empty());

            // WHEN
            String result = templateSearchTools.getTemplateDesignSpecs(templateId);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Template not found");
            verify(specsExtractor, never()).extractSpecs(any());
            verify(specsFormatter, never()).format(any());
        }

        @Test
        @DisplayName("getTemplateDesignSpecs — returns error when templateId is null")
        void getTemplateDesignSpecs_returnsError_whenTemplateIdIsNull() {
            // GIVEN
            String templateId = null;

            // WHEN
            String result = templateSearchTools.getTemplateDesignSpecs(templateId);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Template ID cannot be empty");
            verify(ragTemplateService, never()).getTemplateById(anyString());
        }

        @Test
        @DisplayName("getTemplateDesignSpecs — returns error when templateId is blank")
        void getTemplateDesignSpecs_returnsError_whenTemplateIdIsBlank() {
            // GIVEN
            String templateId = "   ";

            // WHEN
            String result = templateSearchTools.getTemplateDesignSpecs(templateId);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Template ID cannot be empty");
            verify(ragTemplateService, never()).getTemplateById(anyString());
        }

        @Test
        @DisplayName("getTemplateDesignSpecs — returns error when specsExtractor throws exception")
        void getTemplateDesignSpecs_returnsError_whenExtractorThrowsException() {
            // GIVEN
            String templateId = "template-1";
            MarketingTemplate template = buildTemplate(templateId, "email", "Email", List.of());
            when(ragTemplateService.getTemplateById(templateId)).thenReturn(Optional.of(template));
            when(specsExtractor.extractSpecs(template)).thenThrow(new RuntimeException("Extraction failed"));

            // WHEN
            String result = templateSearchTools.getTemplateDesignSpecs(templateId);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Failed to extract specs");
            verify(specsFormatter, never()).format(any());
        }
    }

    // ─── listTemplateTypes ────────────────────────────────────────────────────

    @Nested
    @DisplayName("listTemplateTypes")
    class ListTemplateTypesTests {

        @Test
        @DisplayName("listTemplateTypes — returns types tags and total when service responds")
        void listTemplateTypes_returnsTypesTagsAndTotal_whenServiceResponds() {
            // GIVEN
            Set<String> types = Set.of("social_media_post", "email", "poster_a3");
            Set<String> tags = Set.of("modern", "minimal", "professional");
            when(ragTemplateService.getAvailableTypes()).thenReturn(types);
            when(ragTemplateService.getAvailableTags()).thenReturn(tags);
            when(ragTemplateService.getTemplateCount()).thenReturn(10);

            // WHEN
            String result = templateSearchTools.listTemplateTypes();

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("\"types\"");
            assertThat(result).contains("\"tags\"");
            assertThat(result).contains("\"total_templates\": 10");
            verify(ragTemplateService).getAvailableTypes();
            verify(ragTemplateService).getAvailableTags();
            verify(ragTemplateService).getTemplateCount();
        }

        @Test
        @DisplayName("listTemplateTypes — returns error when service throws exception")
        void listTemplateTypes_returnsError_whenServiceThrowsException() {
            // GIVEN
            when(ragTemplateService.getAvailableTypes()).thenThrow(new RuntimeException("DB error"));

            // WHEN
            String result = templateSearchTools.listTemplateTypes();

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Failed to list types");
            verify(ragTemplateService, never()).getAvailableTags();
            verify(ragTemplateService, never()).getTemplateCount();
        }
    }

    // ─── getTemplatesByType ───────────────────────────────────────────────────

    @Nested
    @DisplayName("getTemplatesByType")
    class GetTemplatesByTypeTests {

        @Test
        @DisplayName("getTemplatesByType — returns formatted templates when type found")
        void getTemplatesByType_returnsFormattedTemplates_whenTypeFound() {
            // GIVEN
            String type = "email";
            MarketingTemplate template = buildTemplate("3", type, "Email promo", List.of("professional"));
            when(ragTemplateService.getTemplatesByType(type)).thenReturn(List.of(template));

            // WHEN
            String result = templateSearchTools.getTemplatesByType(type);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("\"type\"");
            assertThat(result).contains("email");
            verify(ragTemplateService).getTemplatesByType(type);
        }

        @Test
        @DisplayName("getTemplatesByType — returns available types when requested type not found")
        void getTemplatesByType_returnsAvailableTypes_whenTypeNotFound() {
            // GIVEN
            String type = "unknown_type";
            when(ragTemplateService.getTemplatesByType(type)).thenReturn(List.of());
            when(ragTemplateService.getAvailableTypes()).thenReturn(Set.of("email", "poster_a3"));

            // WHEN
            String result = templateSearchTools.getTemplatesByType(type);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("availableTypes");
            verify(ragTemplateService).getAvailableTypes();
        }

        @Test
        @DisplayName("getTemplatesByType — returns error when type is null")
        void getTemplatesByType_returnsError_whenTypeIsNull() {
            // GIVEN
            String type = null;

            // WHEN
            String result = templateSearchTools.getTemplatesByType(type);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Type cannot be empty");
            verify(ragTemplateService, never()).getTemplatesByType(anyString());
        }

        @Test
        @DisplayName("getTemplatesByType — returns error when type is blank")
        void getTemplatesByType_returnsError_whenTypeIsBlank() {
            // GIVEN
            String type = "  ";

            // WHEN
            String result = templateSearchTools.getTemplatesByType(type);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Type cannot be empty");
            verify(ragTemplateService, never()).getTemplatesByType(anyString());
        }

        @Test
        @DisplayName("getTemplatesByType — returns error when service throws exception")
        void getTemplatesByType_returnsError_whenServiceThrowsException() {
            // GIVEN
            String type = "email";
            when(ragTemplateService.getTemplatesByType(type)).thenThrow(new RuntimeException("DB error"));

            // WHEN
            String result = templateSearchTools.getTemplatesByType(type);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Failed to get templates");
        }
    }

    // ─── getTemplatesByTag ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getTemplatesByTag")
    class GetTemplatesByTagTests {

        @Test
        @DisplayName("getTemplatesByTag — returns formatted templates when tag found")
        void getTemplatesByTag_returnsFormattedTemplates_whenTagFound() {
            // GIVEN
            String tag = "modern";
            MarketingTemplate template = buildTemplate("4", "flyer_a5", "Modern flyer", List.of("modern", "colorful"));
            when(ragTemplateService.getTemplatesByTag(tag)).thenReturn(List.of(template));

            // WHEN
            String result = templateSearchTools.getTemplatesByTag(tag);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("\"tag\"");
            assertThat(result).contains("modern");
            verify(ragTemplateService).getTemplatesByTag(tag);
        }

        @Test
        @DisplayName("getTemplatesByTag — returns available tags when requested tag not found")
        void getTemplatesByTag_returnsAvailableTags_whenTagNotFound() {
            // GIVEN
            String tag = "unknown_tag";
            when(ragTemplateService.getTemplatesByTag(tag)).thenReturn(List.of());
            when(ragTemplateService.getAvailableTags()).thenReturn(Set.of("modern", "minimal"));

            // WHEN
            String result = templateSearchTools.getTemplatesByTag(tag);

            // THEN
            assertThat(result).contains("\"success\": false");
            assertThat(result).contains("availableTags");
            verify(ragTemplateService).getAvailableTags();
        }

        @Test
        @DisplayName("getTemplatesByTag — returns error when tag is null")
        void getTemplatesByTag_returnsError_whenTagIsNull() {
            // GIVEN
            String tag = null;

            // WHEN
            String result = templateSearchTools.getTemplatesByTag(tag);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Tag cannot be empty");
            verify(ragTemplateService, never()).getTemplatesByTag(anyString());
        }

        @Test
        @DisplayName("getTemplatesByTag — returns error when tag is blank")
        void getTemplatesByTag_returnsError_whenTagIsBlank() {
            // GIVEN
            String tag = "  ";

            // WHEN
            String result = templateSearchTools.getTemplatesByTag(tag);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Tag cannot be empty");
            verify(ragTemplateService, never()).getTemplatesByTag(anyString());
        }

        @Test
        @DisplayName("getTemplatesByTag — returns error when service throws exception")
        void getTemplatesByTag_returnsError_whenServiceThrowsException() {
            // GIVEN
            String tag = "modern";
            when(ragTemplateService.getTemplatesByTag(tag)).thenThrow(new RuntimeException("DB error"));

            // WHEN
            String result = templateSearchTools.getTemplatesByTag(tag);

            // THEN
            assertThat(result).contains("error");
            assertThat(result).contains("Failed to get templates");
        }

        @Test
        @DisplayName("getTemplatesByTag — handles template with null tags formatted as empty array")
        void getTemplatesByTag_handlesNullTags_formattedAsEmptyArray() {
            // GIVEN
            String tag = "minimal";
            MarketingTemplate template = buildTemplate("5", "poster_a4", "Minimal poster", null);
            when(ragTemplateService.getTemplatesByTag(tag)).thenReturn(List.of(template));

            // WHEN
            String result = templateSearchTools.getTemplatesByTag(tag);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("\"tags\": []");
            verify(ragTemplateService).getTemplatesByTag(tag);
        }

        @Test
        @DisplayName("getTemplatesByTag — handles template with empty tags formatted as empty array")
        void getTemplatesByTag_handlesEmptyTags_formattedAsEmptyArray() {
            // GIVEN
            String tag = "minimal";
            MarketingTemplate template = buildTemplate("6", "poster_a4", "Minimal poster", List.of());
            when(ragTemplateService.getTemplatesByTag(tag)).thenReturn(List.of(template));

            // WHEN
            String result = templateSearchTools.getTemplatesByTag(tag);

            // THEN
            assertThat(result).contains("\"success\": true");
            assertThat(result).contains("\"tags\": []");
            verify(ragTemplateService).getTemplatesByTag(tag);
        }
    }
}