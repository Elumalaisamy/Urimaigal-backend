package com.urimaigal.controller;

import com.urimaigal.dto.ApiResponse;
import com.urimaigal.dto.ChatRequest;
import com.urimaigal.model.ChatMessage;
import com.urimaigal.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST   /api/chat/message      — send a message, receive bot reply
 * GET    /api/chat/history      — get full chat history for user
 * GET    /api/chat/history/recent — get last 50 messages
 * DELETE /api/chat/history      — clear chat history
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ChatRequest request) {
        ChatMessage botReply = chatService.sendMessage(userId, request.getContent());
        return ResponseEntity.ok(ApiResponse.ok(botReply));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getHistory(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.getChatHistory(userId)));
    }

    @GetMapping("/history/recent")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getRecentHistory(
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.getRecentHistory(userId)));
    }

    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> clearHistory(
            @AuthenticationPrincipal String userId) {
        chatService.clearChatHistory(userId);
        return ResponseEntity.ok(ApiResponse.ok("Chat history cleared", null));
    }
}
