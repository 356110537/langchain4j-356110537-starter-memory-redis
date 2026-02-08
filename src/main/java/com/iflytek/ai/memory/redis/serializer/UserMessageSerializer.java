package com.iflytek.ai.memory.redis.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.iflytek.ai.memory.redis.dto.TextContentDTO;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * UserMessage 自定义序列化器
 * 用于将 UserMessage 序列化为 JSON
 */
public class UserMessageSerializer extends JsonSerializer<UserMessage> {

    @Override
    public void serialize(UserMessage userMessage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("type", ChatMessageType.USER);
        jsonGenerator.writeObjectField("name", userMessage.name());
        List<Content> contents = userMessage.contents();
        List<TextContentDTO> list = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(contents)) {
            for (Content item : contents) {
                if (item instanceof TextContent textContent) {
                    if (StringUtils.isNotBlank(textContent.text()) && !"{}".equals(textContent.text())) {
                        list.add(TextContentDTO.from(textContent.text()));
                    }
                }
            }
        }
        jsonGenerator.writeObjectField("contents", list);
        jsonGenerator.writeObjectField("attributes", userMessage.attributes());
        jsonGenerator.writeEndObject();
    }
}