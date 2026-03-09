package com.penpot.ai.application.router;

import com.penpot.ai.core.domain.ToolCategory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("IntentRouterService — Unit")
public class IntentRouterServiceUnit {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient routerChatClient;

    @InjectMocks
    private IntentRouterService intentRouterService;

    @Nested
    @DisplayName("Structured output parsing")
    class StructuredOutputTests {

        @Test
        @DisplayName("route — returns parsed categories when JSON is valid")
        void route_returnsParsedCategoriesWhenJsonIsValid() {

            when(routerChatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("{\"categories\":[\"shape_creation\",\"inspection\"]}");

            Set<ToolCategory> result = intentRouterService.route("create a rectangle");

            assertThat(result).containsExactlyInAnyOrder(
                    ToolCategory.SHAPE_CREATION,
                    ToolCategory.INSPECTION
            );
        }

        @Test
        @DisplayName("route — ignores unknown categories in JSON")
        void route_ignoresUnknownCategoriesInJson() {

            when(routerChatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("{\"categories\":[\"inspection\",\"unknown_category\"]}");

            Set<ToolCategory> result = intentRouterService.route("generate a landing page");

            assertThat(result).containsExactly(ToolCategory.INSPECTION);
        }

        @Test
        @DisplayName("route — returns INSPECTION when JSON contains empty categories")
        void route_returnsInspectionWhenJsonCategoriesEmpty() {

            when(routerChatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn("{\"categories\":[]}");

            Set<ToolCategory> result = intentRouterService.route("something vague");

            assertThat(result).containsExactly(ToolCategory.INSPECTION);
        }
    }

    @Nested
    @DisplayName("Fallback behaviour")
    class FallbackTests {

        @Test
        @DisplayName("route — fallback keyword detection when JSON parsing fails")
        void route_fallbackKeywordDetectionWhenJsonFails() {

            when(routerChatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn(
                            "not a json response",
                            "Use LAYOUT_AND_ALIGNMENT and INSPECTION for this request"
                    );

            Set<ToolCategory> result = intentRouterService.route("align everything on the left");

            assertThat(result).containsExactlyInAnyOrder(
                    ToolCategory.LAYOUT_AND_ALIGNMENT,
                    ToolCategory.INSPECTION
            );
        }

        @Test
        @DisplayName("route — fallback to INSPECTION when keyword detection fails")
        void route_fallbackToInspectionWhenKeywordFails() {

            when(routerChatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenReturn(
                            "invalid json",
                            "random text with no categories"
                    );

            Set<ToolCategory> result = intentRouterService.route("do something");

            assertThat(result).containsExactly(ToolCategory.INSPECTION);
        }

        @Test
        @DisplayName("route — fallback to INSPECTION when LLM call fails")
        void route_fallbackToInspectionWhenExceptionOccurs() {

            when(routerChatClient.prompt().system(anyString()).user(anyString()).call().content())
                    .thenThrow(new RuntimeException("LLM unavailable"));

            Set<ToolCategory> result = intentRouterService.route("delete header");

            assertThat(result).isEqualTo(EnumSet.of(ToolCategory.INSPECTION));
        }
    }
}