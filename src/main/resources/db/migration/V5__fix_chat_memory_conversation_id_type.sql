ALTER TABLE spring_ai_chat_memory 
ALTER COLUMN conversation_id TYPE TEXT 
USING conversation_id::TEXT;