package com.iflytek.ai.memory.redis.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

import java.io.IOException;

/**
 * @Author wudawei
 * @Email 356110537@qq.com
 * @Date 2026/1/3  15:49
 */
public class ToolExecutionResultMessageSerializer extends JsonSerializer<ToolExecutionResultMessage> {

    @Override
    public void serialize(ToolExecutionResultMessage toolMessage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("type", ChatMessageType.TOOL_EXECUTION_RESULT);
        jsonGenerator.writeObjectField("id", toolMessage.id());
        jsonGenerator.writeObjectField("toolName", toolMessage.toolName());
        jsonGenerator.writeObjectField("text", toolMessage.text());
        jsonGenerator.writeEndObject();
    }
}