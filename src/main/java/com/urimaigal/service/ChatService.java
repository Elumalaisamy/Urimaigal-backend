package com.urimaigal.service;

import com.urimaigal.model.ChatMessage;
import com.urimaigal.repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatMessageRepository chatMessageRepository;
    private final JmsTemplate jmsTemplate;
    private final String chatQueue;

    public ChatService(ChatMessageRepository chatMessageRepository,
                       JmsTemplate jmsTemplate,
                       @Value("${app.jms.chat-queue}") String chatQueue) {
        this.chatMessageRepository = chatMessageRepository;
        this.jmsTemplate = jmsTemplate;
        this.chatQueue = chatQueue;
    }

    // ==========================================
    // Chat history
    // ==========================================

    public List<ChatMessage> getChatHistory(String userId) {
        return chatMessageRepository.findByUserId(userId);
    }

    public List<ChatMessage> getRecentHistory(String userId) {
        return chatMessageRepository.findByUserIdLimited(userId, 50);
    }

    public void clearChatHistory(String userId) {
        chatMessageRepository.deleteByUserId(userId);
        log.info("Cleared chat history for user '{}'", userId);
    }

    // ==========================================
    // Send message — persist user msg immediately,
    // generate bot response, queue bot msg async
    // ==========================================

    public ChatMessage sendMessage(String userId, String content) {
        // 1. Persist user message synchronously
        ChatMessage userMessage = buildMessage(userId, "user", content, null);
        chatMessageRepository.save(userMessage);

        // 2. Generate bot response
        BotResponse botResponse = generateBotResponse(content);
        ChatMessage botMessage = buildMessage(userId, "bot", botResponse.content(), botResponse.suggestions());

        // 3. Persist bot message asynchronously via JMS
        try {
            jmsTemplate.convertAndSend(chatQueue, botMessage);
        } catch (Exception e) {
            // Fallback: persist synchronously if JMS is unavailable
            log.warn("JMS unavailable for chat queue; persisting bot message synchronously: {}", e.getMessage());
            chatMessageRepository.save(botMessage);
        }

        log.debug("Chat message processed for user '{}': userMsgId={}, botMsgId={}",
                userId, userMessage.getId(), botMessage.getId());

        return botMessage;
    }

    // ==========================================
    // Bot response engine (mirrors frontend logic)
    // ==========================================

    private BotResponse generateBotResponse(String message) {
        String msg = message.toLowerCase();

        if (containsAny(msg, "property", "land", "house", "rent", "patta", "encumbrance")) {
            return new BotResponse(
                    "Property disputes in India are governed by the Transfer of Property Act, 1882.\n\n" +
                    "Key steps:\n" +
                    "• Verify all documents — sale deed, encumbrance certificate, patta\n" +
                    "• File in Civil Court (under ₹3L) or District Court (above)\n" +
                    "• Seek an injunction to prevent transfer during dispute\n" +
                    "• Consider mediation under the Legal Services Authority\n\n" +
                    "Would you like me to recommend a Civil or Property law specialist?",
                    List.of("Find a property lawyer", "What documents do I need?", "How long does it take?")
            );
        }

        if (containsAny(msg, "criminal", "arrest", "fir", "police", "bail", "custody")) {
            return new BotResponse(
                    "In a criminal matter your immediate rights include:\n\n" +
                    "• Right to know grounds of arrest (Article 22)\n" +
                    "• Right to be produced before a magistrate within 24 hours\n" +
                    "• Right to bail (for bailable offences)\n" +
                    "• Right to free legal aid if unable to afford counsel\n\n" +
                    "Act quickly — the first 24 hours are critical. Would you like to connect with a criminal defence advocate?",
                    List.of("Connect with criminal lawyer", "FIR registration help", "Bail procedure")
            );
        }

        if (containsAny(msg, "family", "divorce", "custody", "maintenance", "domestic")) {
            return new BotResponse(
                    "Family law matters in India:\n\n" +
                    "• Mutual consent divorce takes 6–18 months\n" +
                    "• Contested divorce can take longer; interim relief is available\n" +
                    "• Protection orders under Domestic Violence Act can be obtained quickly\n" +
                    "• Child custody is decided based on the best interest of the child\n\n" +
                    "Would you like to speak to a family law specialist?",
                    List.of("Find family lawyer", "Child custody rights", "Maintenance procedure")
            );
        }

        if (containsAny(msg, "job", "employ", "work", "termination", "salary", "posh", "labour")) {
            return new BotResponse(
                    "For employment disputes in India:\n\n" +
                    "• Wrongful termination: File complaint with Labour Commissioner\n" +
                    "• Non-payment of dues: Approach Labour Court under Industrial Disputes Act\n" +
                    "• Sexual harassment: POSH Act applies to companies with 10+ employees\n" +
                    "• PF/ESI disputes: Approach EPFO directly or file a claim\n\n" +
                    "Would you like to connect with a Labour Law specialist?",
                    List.of("Find labour lawyer", "Termination rights", "POSH Act complaint")
            );
        }

        if (containsAny(msg, "traffic", "fine", "challan", "driving", "vehicle", "speed")) {
            return new BotResponse(
                    "Traffic violation fines under the Motor Vehicles (Amendment) Act 2019:\n\n" +
                    "• Driving without licence: ₹5,000\n" +
                    "• Without insurance: ₹2,000\n" +
                    "• Without helmet: ₹1,000 + 3-month suspension\n" +
                    "• Drunk driving: ₹10,000 and/or 6 months imprisonment\n\n" +
                    "You can contest a challan before the Magistrate within 60 days.",
                    List.of("Contest a challan", "Find a traffic/criminal lawyer")
            );
        }

        if (containsAny(msg, "corporate", "company", "startup", "contract", "merger", "mca")) {
            return new BotResponse(
                    "Corporate legal matters in India:\n\n" +
                    "• Company registration under Companies Act 2013 via MCA portal\n" +
                    "• Startup recognition under DPIIT for tax benefits\n" +
                    "• Commercial contracts must clearly define scope, payment, and dispute resolution\n" +
                    "• M&A transactions require due diligence and NCLT approval\n\n" +
                    "Would you like to connect with a Corporate law specialist?",
                    List.of("Find corporate lawyer", "Startup registration", "Contract review")
            );
        }

        if (containsAny(msg, "immigration", "visa", "passport", "oci", "pr", "work permit")) {
            return new BotResponse(
                    "Immigration legal assistance:\n\n" +
                    "• Work visas: H1B, UK Skilled Worker, Canada Express Entry\n" +
                    "• OCI card: for persons of Indian origin abroad\n" +
                    "• Student visa: F1 (US), Tier 4 (UK), study permit (Canada)\n" +
                    "• PR applications can take 1–3 years depending on country\n\n" +
                    "Would you like to speak to an Immigration specialist?",
                    List.of("Find immigration lawyer", "Visa application help", "OCI card process")
            );
        }

        // Default
        return new BotResponse(
                "வணக்கம்! I understand you have a legal concern. Could you describe your situation in more detail?\n\n" +
                "I can help you with property disputes, criminal matters, family law, employment issues, immigration, corporate law, and more.",
                List.of("Property dispute", "Criminal case", "Family matter", "Employment issue", "Traffic fine")
        );
    }

    // ==========================================
    // Helpers
    // ==========================================

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    private ChatMessage buildMessage(String userId, String role, String content, List<String> suggestions) {
        ChatMessage msg = new ChatMessage();
        msg.setId("msg-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        msg.setUserId(userId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setSuggestions(suggestions);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }

    private record BotResponse(String content, List<String> suggestions) {}
}
