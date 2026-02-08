package com.iflytek.ai.memory.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.iflytek.ai.memory.redis.serializer.*;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.Assert;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.JedisPooled;

import java.util.ArrayList;
import java.util.List;

public class RedisChatMemoryRepository implements ChatMemoryStore, AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String defaultKeyPrefix;
    private final JedisPooled jedisPool;
    private final ObjectMapper objectMapper;

    public RedisChatMemoryRepository(JedisConnectionFactory jedisConnectionFactory, ObjectMapper objectMapper, String redisPrefix) {
        this.jedisPool = this.jedisPooled(jedisConnectionFactory);
        this.objectMapper = objectMapper;
        this.defaultKeyPrefix = redisPrefix;
        SimpleModule module = new SimpleModule("langChain4jMessageModule");
        module.addSerializer(UserMessage.class, new UserMessageSerializer());
        module.addSerializer(AiMessage.class, new AiMessageSerializer());
        module.addSerializer(SystemMessage.class, new SystemMessageSerializer());
        module.addSerializer(ToolExecutionResultMessage.class, new ToolExecutionResultMessageSerializer());
        module.addSerializer(CustomMessage.class, new CustomMessageSerializer());
        module.addDeserializer(ChatMessage.class, new MessageDeserializer());
        this.objectMapper.registerModule(module);
    }

    private JedisPooled jedisPooled(JedisConnectionFactory jedisConnectionFactory) {
        String host = jedisConnectionFactory.getHostName();
        int port = jedisConnectionFactory.getPort();
        JedisClientConfig clientConfig = DefaultJedisClientConfig.builder().ssl(jedisConnectionFactory.isUseSsl()).clientName(jedisConnectionFactory.getClientName()).timeoutMillis(jedisConnectionFactory.getTimeout()).password(jedisConnectionFactory.getPassword()).build();
        return new JedisPooled(new HostAndPort(host, port), clientConfig);
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Assert.notNull(memoryId, "memoryId cannot be null or empty");
        String key = defaultKeyPrefix + memoryId;
        List<String> messageStrings = jedisPool.lrange(key, 0, -1);
        List<ChatMessage> messages = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(messageStrings)) {
            for (String messageString : messageStrings) {
                try {
                    ChatMessage message = objectMapper.readValue(messageString, ChatMessage.class);
                    if (message != null && message.type() != null) {
                        messages.add(message);
                    }
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error deserializing message", e);
                }
            }
        }
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Assert.notNull(memoryId, "memoryId cannot be null or empty");
        Assert.notNull(messages, "messages cannot be null");
        Assert.noNullElements(messages, "messages cannot contain null elements");
        // Clear existing messages first
        this.deleteMessages(memoryId);
        // Add all messages in order
        String key = defaultKeyPrefix + memoryId;
        for (ChatMessage message : messages) {
            try {
                switch (message) {
                    case UserMessage userMessage: {
                        List<Content> contents = userMessage.contents();
                        if (ObjectUtils.isNotEmpty(contents)) {
                            for (Content content : contents) {
                                if (content instanceof TextContent textContent) {
                                    if (StringUtils.isNotBlank(textContent.text()) && !"{}".equals(textContent.text())) {
                                        String messageJson = objectMapper.writeValueAsString(message);
                                        jedisPool.rpush(key, messageJson);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case AiMessage aiMessage: {
                        String messageJson = objectMapper.writeValueAsString(aiMessage);
                        jedisPool.rpush(key, messageJson);
                        break;
                    }
                    case SystemMessage systemMessage: {
                        String messageJson = objectMapper.writeValueAsString(systemMessage);
                        jedisPool.rpush(key, messageJson);
                        break;
                    }
                    case ToolExecutionResultMessage tooMessage: {
                        String messageJson = objectMapper.writeValueAsString(tooMessage);
                        jedisPool.rpush(key, messageJson);
                        break;
                    }
                    case CustomMessage customMessage: {
                        String messageJson = objectMapper.writeValueAsString(customMessage);
                        jedisPool.rpush(key, messageJson);
                        break;
                    }
                    default: {
                        // Other types will not be processed.
                    }
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing message", e);
            }
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Assert.notNull(memoryId, "memoryId cannot be null or empty");
        String key = defaultKeyPrefix + memoryId;
        jedisPool.del(key);
    }

    @Override
    public void close() throws Exception {
        if (jedisPool != null) {
            jedisPool.close();
            logger.info("Redis connection pool closed");
        }
    }
}