package com.iflytek.ai.memory.redis.dto;

import dev.langchain4j.data.message.ContentType;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author wudawei
 * @Email 356110537@qq.com
 * @Date 2026/1/4  22:20
 */
public class TextContentDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7419343079906031648L;

    private String text;

    public TextContentDTO() {
    }

    public TextContentDTO(String text) {
        this.text = text;
    }

    public static TextContentDTO from(String text) {
        return new TextContentDTO(text);
    }

    public ContentType type() {
        return ContentType.TEXT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}