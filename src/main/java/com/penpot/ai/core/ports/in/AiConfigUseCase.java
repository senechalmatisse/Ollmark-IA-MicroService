package com.penpot.ai.core.ports.in;

import java.util.Map;

public interface AiConfigUseCase {
    Map<String, Object> getConfig(String projectId);
    void updateConfig(String projectId, Map<String, Object> config);
    String getPrompt(String projectId);
    void updatePrompt(String projectId, String prompt);
}
