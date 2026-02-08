package com.iflytek.ai.memory.redis.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author wudawei
 * @Email 356110537@qq.com
 * @Date 2026/1/4  22:20
 */
public class ToolExecutionRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1712304449674001520L;

    private String id;
    private String name;
    private String arguments;

    public ToolExecutionRequestDTO() {
    }

    public ToolExecutionRequestDTO(String id, String name, String arguments) {
        this.id = id;
        this.name = name;
        this.arguments = arguments;
    }

    public static ToolExecutionRequestDTO from(String id, String name, String arguments) {
        return new ToolExecutionRequestDTO(id, name, arguments);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}