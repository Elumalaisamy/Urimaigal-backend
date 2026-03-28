package com.urimaigal.repository;

import com.urimaigal.model.User;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    public UserRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setPhone(rs.getString("phone"));
            user.setAvatar(rs.getString("avatar"));
            user.setRole(rs.getString("role"));
            user.setCreatedAt(rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            user.setUpdatedAt(rs.getTimestamp("updated_at") != null
                    ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return user;
        }
    };

    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = :id";
        List<User> results = namedJdbc.query(sql, new MapSqlParameterSource("id", id), USER_ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = :email";
        List<User> results = namedJdbc.query(sql, new MapSqlParameterSource("email", email), USER_ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = :email";
        Integer count = namedJdbc.queryForObject(sql, new MapSqlParameterSource("email", email), Integer.class);
        return count != null && count > 0;
    }

    public int save(User user) {
        String sql = """
                INSERT INTO users (id, name, email, password, phone, avatar, role)
                VALUES (:id, :name, :email, :password, :phone, :avatar, :role)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("name", user.getName())
                .addValue("email", user.getEmail())
                .addValue("password", user.getPassword())
                .addValue("phone", user.getPhone())
                .addValue("avatar", user.getAvatar())
                .addValue("role", user.getRole() != null ? user.getRole() : "USER");
        return namedJdbc.update(sql, params);
    }

    public int update(User user) {
        String sql = """
                UPDATE users
                SET name = :name, phone = :phone, avatar = :avatar
                WHERE id = :id
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", user.getName())
                .addValue("phone", user.getPhone())
                .addValue("avatar", user.getAvatar())
                .addValue("id", user.getId());
        return namedJdbc.update(sql, params);
    }
}
