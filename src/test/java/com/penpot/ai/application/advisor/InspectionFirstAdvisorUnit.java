package com.penpot.ai.application.advisor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.penpot.ai.application.tools.PenpotInspectorTools;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;

import java.util.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InspectionFirstAdvisor — Unit")
class InspectionFirstAdvisorUnit {

    @Mock
    private PenpotInspectorTools inspectorTools;

    @Mock
    private CallAdvisorChain chain;

    @Mock
    private ChatClientRequest request;

    @Mock
    private ChatClientResponse response;

    @Mock
    private Prompt prompt;

    @Mock
    private Prompt augmentedPrompt;

    private InspectionFirstAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new InspectionFirstAdvisor(inspectorTools);
    }

    // ─────────────────────────────────────────────────────────────
    // getName / getOrder
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getName / getOrder")
    class MetadataTests {

        @Test
        @DisplayName("getName — returns 'InspectionFirstAdvisor'")
        void getName_returnsCorrectName() {
            assertThat(advisor.getName()).isEqualTo("InspectionFirstAdvisor");
        }

        @Test
        @DisplayName("getOrder — returns HIGHEST_PRECEDENCE + 50")
        void getOrder_returnsHighestPrecedencePlus50() {
            assertThat(advisor.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 50);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // adviseCall — injection du contexte
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("adviseCall — injection context")
    class AdviseCallTests {

        @Test
        @DisplayName("adviseCall — injects page context when INSPECTION category is present")
        void adviseCall_injectsContextWhenInspectionCategoryPresent() {
            // GIVEN
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, List.of("INSPECTION"));

            when(request.context()).thenReturn(context);
            when(request.prompt()).thenReturn(prompt);
            when(inspectorTools.getPageContext("compact")).thenReturn("{\"shapes\": []}");
            when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
            when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools).getPageContext("compact");
            verify(prompt).augmentSystemMessage(anyString());
            verify(chain).nextCall(any(ChatClientRequest.class));
        }

        @Test
        @DisplayName("adviseCall — injects page context when COLOR_AND_STYLE category is present")
        void adviseCall_injectsContextWhenColorAndStyleCategoryPresent() {
            // GIVEN
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, List.of("COLOR_AND_STYLE"));

            when(request.context()).thenReturn(context);
            when(request.prompt()).thenReturn(prompt);
            when(inspectorTools.getPageContext("compact")).thenReturn("{\"shapes\": []}");
            when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
            when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools).getPageContext("compact");
        }

        @Test
        @DisplayName("adviseCall — injects page context when SHAPE_MODIFICATION category is present")
        void adviseCall_injectsContextWhenShapeModificationCategoryPresent() {
            // GIVEN
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, List.of("SHAPE_MODIFICATION"));

            when(request.context()).thenReturn(context);
            when(request.prompt()).thenReturn(prompt);
            when(inspectorTools.getPageContext("compact")).thenReturn("{\"shapes\": []}");
            when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
            when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools).getPageContext("compact");
        }

        @Test
        @DisplayName("adviseCall — injects page context when DELETION category is present")
        void adviseCall_injectsContextWhenDeletionCategoryPresent() {
            // GIVEN
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, List.of("DELETION"));

            when(request.context()).thenReturn(context);
            when(request.prompt()).thenReturn(prompt);
            when(inspectorTools.getPageContext("compact")).thenReturn("{\"shapes\": []}");
            when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
            when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools).getPageContext("compact");
        }

        @Test
        @DisplayName("adviseCall — skips injection when already injected")
        void adviseCall_skipsInjectionWhenAlreadyInjected() {
            // GIVEN
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, List.of("INSPECTION"));
            context.put("inspectionInjected", true);

            when(request.context()).thenReturn(context);
            when(chain.nextCall(request)).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools, never()).getPageContext(anyString());
            verify(chain).nextCall(request);
        }

        @Test
        @DisplayName("adviseCall — skips injection when no relevant category")
        void adviseCall_skipsInjectionWhenNoRelevantCategory() {
            // GIVEN
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, List.of("OTHER_CATEGORY"));

            when(request.context()).thenReturn(context);
            when(chain.nextCall(request)).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools, never()).getPageContext(anyString());
            verify(chain).nextCall(request);
        }

        @Test
        @DisplayName("adviseCall — skips injection when no categories in context")
        void adviseCall_skipsInjectionWhenNoCategoriesInContext() {
            // GIVEN
            Map<String, Object> context = new HashMap<>();

            when(request.context()).thenReturn(context);
            when(chain.nextCall(request)).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools, never()).getPageContext(anyString());
        }
    }

    // ────────────────────Categorie─────────────────────────────────────────
    // extractCategories — formats supportés
    // ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("extractCategories — supported formats")
    class ExtractCategoriesTests {

        @Test
        @DisplayName("extractCategories — handles single string value (fallback)")
        void extractCategories_handlesSingleStringValue() {
            // GIVEN — catégorie passée comme simple String (pas une Collection)
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, "INSPECTION");

            when(request.context()).thenReturn(context);
            when(request.prompt()).thenReturn(prompt);
            when(inspectorTools.getPageContext("compact")).thenReturn("{\"shapes\": []}");
            when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
            when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools).getPageContext("compact");
        }

        @Test
        @DisplayName("extractCategories — handles null categories (returns empty set)")
        void extractCategories_handlesNullCategories() {
            // GIVEN — pas de catégories dans le contexte
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES, null);

            when(request.context()).thenReturn(context);
            when(chain.nextCall(request)).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools, never()).getPageContext(anyString());
        }

        @Test
        @DisplayName("extractCategories — handles multiple categories in collection")
        void extractCategories_handlesMultipleCategoriesInCollection() {
            // GIVEN — plusieurs catégories dont INSPECTION
            Map<String, Object> context = new HashMap<>();
            context.put(InspectionFirstAdvisor.CTX_TOOL_CATEGORIES,
                    List.of("OTHER", "INSPECTION", "COLOR_AND_STYLE"));

            when(request.context()).thenReturn(context);
            when(request.prompt()).thenReturn(prompt);
            when(inspectorTools.getPageContext("compact")).thenReturn("{\"shapes\": []}");
            when(prompt.augmentSystemMessage(anyString())).thenReturn(augmentedPrompt);
            when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

            // WHEN
            ChatClientResponse result = advisor.adviseCall(request, chain);

            // THEN
            assertThat(result).isSameAs(response);
            verify(inspectorTools).getPageContext("compact");
        }
    }
}