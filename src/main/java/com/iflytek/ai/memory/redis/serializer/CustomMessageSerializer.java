package com.iflytek.ai.memory.redis.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.CustomMessage;

import java.io.IOException;

/**
 * @Author wudawei
 * @Email 356110537@qq.com
 * @Date 2026/1/3  17:05
 */
public class CustomMessageSerializer extends JsonSerializer<CustomMessage> {

    @Override
    public void serialize(CustomMessage customMessage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("type", ChatMessageType.CUSTOM);
        jsonGenerator.writeObjectField("attributes", customMessage.attributes());
        jsonGenerator.writeEndObject();
    }
}