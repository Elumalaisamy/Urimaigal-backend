package com.urimaigal.repository;

import com.urimaigal.model.ChatMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ChatMessageRepository {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageRepository.class);

    private final NamedParameterJdbcTemplate namedJdbc;
    private final ObjectMapper objectMapper;

    public ChatMessageRepository(NamedParameterJdbcTemplate namedJdbc, ObjectMapper objectMapper) {
        this.namedJdbc = namedJdbc;
        this.objectMapper = objectMapper;
    }

    private final RowMapper<ChatMessage> CHAT_ROW_MAPPER = new RowMapper<>() {
        @Override
        public ChatMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            ChatMessage msg = new ChatMessage();
            msg.setId(rs.getString("id"));
            msg.setUserId(rs.getString("user_id"));
            msg.setRole(rs.getString("role"));
            msg.setContent(rs.getString("content"));
            String suggestionsJson = rs.getString("suggestions");
            if (suggestionsJson != null && !suggestionsJson.isBlank()) {
                try {
                    msg.setSuggestions(objectMapper.readValue(suggestionsJson, new TypeReference<List<String>>() {}));
                } catch (Exception e) {
                    log.warn("Failed to parse suggestions JSON for message {}: {}", msg.getId(), e.getMessage());
                }
            }
            msg.setCreatedAt(rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            return msg;
        }
    };

    public List<ChatMessage> findByUserId(String userId) {
        String sql = """
                SELECT * FROM chat_messages
                WHERE user_id = :userId
                ORDER BY created_at ASC
                """;
        return namedJdbc.query(sql, new MapSqlParameterSource("userId", userId), CHAT_ROW_MAPPER);
    }

    public List<ChatMessage> findByUserIdLimited(String userId, int limit) {
        String sql = """
                SELECT * FROM (
                    SELECT * FROM chat_messages
                    WHERE user_id = :userId
                    ORDER BY created_at DESC
                    LIMIT :limit
                ) sub ORDER BY created_at ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("limit", limit);
        return namedJdbc.query(sql, params, CHAT_ROW_MAPPER);
    }

    public int save(ChatMessage message) {
        String suggestionsJson = null;
        if (message.getSuggestions() != null && !message.getSuggestions().isEmpty()) {
            try {
                suggestionsJson = objectMapper.writeValueAsString(message.getSuggestions());
            } catch (Exception e) {
                log.warn("Failed to serialize suggestions for message {}: {}", message.getId(), e.getMessage());
            }
        }
        String sql = """
                INSERT INTO chat_messages (id, user_id, role, content, suggestions)
                VALUES (:id, :userId, :role, :content, :suggestions)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", message.getId())
                .addValue("userId", message.getUserId())
                .addValue("role", message.getRole())
                .addValue("content", message.getContent())
                .addValue("suggestions", suggestionsJson);
        return namedJdbc.update(sql, params);
    }

    public int deleteByUserId(String userId) {
        String sql = "DELETE FROM chat_messages WHERE user_id = :userId";
        return namedJdbc.update(sql, new MapSqlParameterSource("userId", userId));
    }
}
