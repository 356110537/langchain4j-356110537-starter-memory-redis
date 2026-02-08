package com.iflytek.ai.memory.redis.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;

import java.io.IOException;

public class SystemMessageSerializer extends JsonSerializer<SystemMessage> {

    @Override
    public void serialize(SystemMessage systemMessage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("type", ChatMessageType.SYSTEM);
        jsonGenerator.writeObjectField("text", systemMessage.text());
        jsonGenerator.writeEndObject();
    }
}