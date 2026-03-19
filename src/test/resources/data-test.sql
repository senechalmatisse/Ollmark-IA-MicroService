INSERT INTO
    projects (id, name)
VALUES (
        '11111111-1111-1111-1111-111111111111',
        'Test Project'
    ) ON CONFLICT DO NOTHING;

INSERT INTO
    ai_model_config (
        project_id,
        prompt_content,
        model_name,
        provider,
        parameters
    )
VALUES (
        '11111111-1111-1111-1111-111111111111',
        'You are a helpful assistant',
        'qwen3:8b',
        'ollama',
        '{}'
    ) ON CONFLICT DO NOTHING;