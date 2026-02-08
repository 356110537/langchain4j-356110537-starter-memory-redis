package com.iflytek.ai.memory.redis.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.iflytek.ai.memory.redis.dto.ToolExecutionRequestDTO;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AiMessageSerializer extends JsonSerializer<AiMessage> {

    @Override
    public void serialize(AiMessage aiMessage, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeObjectField("type", ChatMessageType.AI);
        jsonGenerator.writeObjectField("text", aiMessage.text());
        jsonGenerator.writeObjectField("thinking", aiMessage.thinking());
        List<ToolExecutionRequest> requests = aiMessage.toolExecutionRequests();
        List<ToolExecutionRequestDTO> list = new ArrayList<>(requests.size());
        if (ObjectUtils.isNotEmpty(requests)) {
            for (ToolExecutionRequest item : requests) {
                if (item != null && item.id() != null && StringUtils.isNotBlank(item.name())) {
                    ToolExecutionRequestDTO requestDTO =  ToolExecutionRequestDTO.from(item.id(), item.name(), item.arguments());
                    list.add(requestDTO);
                }
            }
        }
        jsonGenerator.writeObjectField("toolExecutionRequests", list);
        jsonGenerator.writeObjectField("attributes", aiMessage.attributes());
        jsonGenerator.writeEndObject();
    }
}