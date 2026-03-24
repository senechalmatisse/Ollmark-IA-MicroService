package com.penpot.ai.application.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.*;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ToolRetryLimiterAdvisor implements CallAdvisor {

    static final String RETRY_COUNTER = "toolRetryCount";
    static final int MAX_RETRIES = 2;

    private static final String STOP_PROMPT = """
        TOOL RETRY LIMIT REACHED
        The previous tool failed multiple times.
        Do NOT retry the same tool again.
        Instead:
        - try another tool
        - or explain the issue to the user.
        """;

    @Override
    public ChatClientResponse adviseCall(
        ChatClientRequest request,
        CallAdvisorChain chain
    ) {
        Map<String, Object> context = new HashMap<>(request.context());
        int retryCount = (int) context.getOrDefault(RETRY_COUNTER, 0);
        boolean hasError = Boolean.TRUE.equals(context.get("toolErrorDetected"));

        if (hasError && retryCount >= MAX_RETRIES) {
            log.warn("[ToolRetryLimiterAdvisor] Retry limit reached after {} attempts", retryCount);
            Prompt augmentedPrompt = request.prompt().augmentSystemMessage(STOP_PROMPT);
            return chain.nextCall(new ChatClientRequest(augmentedPrompt, context));
        }

        if (hasError) {
            context.put(RETRY_COUNTER, retryCount + 1);
            log.info("[ToolRetryLimiterAdvisor] Retry attempt {}/{}", retryCount + 1, MAX_RETRIES);
        }

        return chain.nextCall(new ChatClientRequest(request.prompt(), context));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 160;
    }

    @Override
    public String getName() {
        return "ToolRetryLimiterAdvisor";
    }
}