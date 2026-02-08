/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iflytek.ai.memory.redis.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Custom JSON deserializer for Message objects
 */
public class MessageDeserializer extends JsonDeserializer<ChatMessage> {
    private static final Logger logger = LoggerFactory.getLogger(MessageDeserializer.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ChatMessage deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (logger.isDebugEnabled()) {
            logger.debug("Deserializing message: {}", node);
        }
        // If node is plain text, create a UserMessage by default
        if (node.isTextual()) {
            return new UserMessage(node.asText());
        }
        // Extract message type
        ChatMessageType type = extractMessageType(node);
        if (type == null) return null;
        switch (type) {
            case USER: {
                String name = node.get("name").textValue();
                List<Content> contents = extractContent(node);
                Map<String, Object> attributes = mapper.convertValue(node.get("attributes"), new TypeReference<>() {
                });
                return UserMessage.builder().name(name).contents(contents).attributes(attributes).build();
            }
            case AI: {
                String text = node.get("text").asText();
                String thinking = node.get("thinking").asText();
                List<ToolExecutionRequest> toolExecutionRequests = extractToolExecutionRequests(node);
                Map<String, Object> attributes = mapper.convertValue(node.get("attributes"), new TypeReference<>() {
                });
                return AiMessage.builder().text(text).thinking(thinking).toolExecutionRequests(toolExecutionRequests).attributes(attributes).build();
            }
            case SYSTEM: {
                String text = node.get("text").asText();
                return SystemMessage.from(text);
            }
            case TOOL_EXECUTION_RESULT: {
                String id = node.get("id").asText();
                String toolName = node.get("toolName").asText();
                String text = node.get("text").asText();
                return ToolExecutionResultMessage.from(id, toolName, text);
            }
            case CUSTOM: {
                Map<String, Object> attributes = mapper.convertValue(node.get("attributes"), new TypeReference<>() {
                });
                return CustomMessage.from(attributes);
            }
        }
        return null;
    }

    /**
     * Extract message type from JsonNode
     */
    private ChatMessageType extractMessageType(JsonNode node) {
        return Optional.ofNullable(node.get("messageType")).map(item -> ChatMessageType.valueOf(item.asText().toUpperCase())).orElseGet(() -> Optional.ofNullable(node.get("type")).map(item -> ChatMessageType.valueOf(item.asText().toUpperCase())).orElseGet(() -> Optional.ofNullable(node.get("role")).map(item -> ChatMessageType.valueOf(item.asText().toUpperCase())).orElse(null)));
    }

    /**
     * Extract message content from JsonNode
     */
    private List<Content> extractContent(JsonNode node) {
        List<Content> list = new ArrayList<>();
        JsonNode jsonNode = node.get("contents");
        if (jsonNode != null && jsonNode.isArray()) {
            for (JsonNode item : jsonNode) {
                JsonNode text = item.get("text");
                if (StringUtils.isNotBlank(text.asText())) {
                    TextContent content = TextContent.from(text.asText());
                    list.add(content);
                }
            }
        }
        return list;
    }

    /**
     * Extract message ToolExecutionRequest from JsonNode
     */
    private List<ToolExecutionRequest> extractToolExecutionRequests(JsonNode node) {
        List<ToolExecutionRequest> list = new ArrayList<>();
        JsonNode jsonNode = node.get("toolExecutionRequests");
        if (jsonNode != null && jsonNode.isArray()) {
            for (JsonNode item : jsonNode) {
                String id = item.get("id").asText();
                String name = item.get("name").asText();
                String arguments = item.get("arguments").asText();
                ToolExecutionRequest request = ToolExecutionRequest.builder().id(id).name(name).arguments(arguments).build();
                list.add(request);
            }
        }
        return list;
    }
}