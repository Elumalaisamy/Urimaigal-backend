package com.urimaigal.messaging;

import com.urimaigal.model.ChatMessage;
import com.urimaigal.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * Asynchronous consumer for chat messages.
 * Persists bot responses to the DB without blocking the HTTP response.
 */
@Component
public class ChatMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageConsumer.class);

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageConsumer(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @JmsListener(destination = "${app.jms.chat-queue}",
                 containerFactory = "jmsListenerContainerFactory")
    public void processChatMessage(ChatMessage message) {
        log.debug("Persisting chat message asynchronously: id={}, user={}, role={}",
                message.getId(), message.getUserId(), message.getRole());
        try {
            chatMessageRepository.save(message);
            log.debug("Chat message '{}' persisted successfully", message.getId());
        } catch (Exception e) {
            log.error("Failed to persist chat message '{}': {}", message.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
