package com.urimaigal.model;

import java.time.LocalDateTime;
import java.util.List;

public class ChatMessage {

    private String id;
    private String userId;
    private String role; // user | bot
    private String content;
    private List<String> suggestions;
    private LocalDateTime createdAt;

    public ChatMessage() {}

    public ChatMessage(String id, String userId, String role, String content,
                       List<String> suggestions, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.content = content;
        this.suggestions = suggestions;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
