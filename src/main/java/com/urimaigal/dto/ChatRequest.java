package com.urimaigal.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ChatRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    public ChatRequest() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
