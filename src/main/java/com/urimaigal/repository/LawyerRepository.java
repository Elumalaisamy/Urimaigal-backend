package com.urimaigal.repository;

import com.urimaigal.model.Lawyer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class LawyerRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    public LawyerRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    // ==========================================
    // RowMapper (no Lombok — explicit)
    // ==========================================
    private static final RowMapper<Lawyer> LAWYER_ROW_MAPPER = new RowMapper<>() {
        @Override
        public Lawyer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Lawyer lawyer = new Lawyer();
            lawyer.setId(rs.getString("id"));
            lawyer.setName(rs.getString("name"));
            lawyer.setAvatar(rs.getString("avatar"));
            lawyer.setSpecialization(rs.getString("specialization"));
            lawyer.setExperience(rs.getInt("experience"));
            lawyer.setRating(rs.getDouble("rating"));
            lawyer.setReviewCount(rs.getInt("review_count"));
            lawyer.setLocation(rs.getString("location"));
            // languages stored as comma-separated string
            String langs = rs.getString("languages");
            lawyer.setLanguages(langs != null ? Arrays.asList(langs.split(",")) : List.of());
            lawyer.setConsultationFee(rs.getBigDecimal("consultation_fee"));
            lawyer.setBio(rs.getString("bio"));
            lawyer.setAvailable(rs.getBoolean("available"));
            lawyer.setBadge(rs.getString("badge"));
            lawyer.setCreatedAt(rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            lawyer.setUpdatedAt(rs.getTimestamp("updated_at") != null
                    ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return lawyer;
        }
    };

    // ==========================================
    // Queries
    // ==========================================

    public List<Lawyer> findAll() {
        String sql = "SELECT * FROM lawyers ORDER BY rating DESC";
        return namedJdbc.query(sql, LAWYER_ROW_MAPPER);
    }

    public Optional<Lawyer> findById(String id) {
        String sql = "SELECT * FROM lawyers WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Lawyer> results = namedJdbc.query(sql, params, LAWYER_ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Lawyer> findBySpecialization(String specialization) {
        String sql = "SELECT * FROM lawyers WHERE specialization = :specialization ORDER BY rating DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("specialization", specialization);
        return namedJdbc.query(sql, params, LAWYER_ROW_MAPPER);
    }

    public List<Lawyer> findAvailable() {
        String sql = "SELECT * FROM lawyers WHERE available = true ORDER BY rating DESC";
        return namedJdbc.query(sql, new MapSqlParameterSource(), LAWYER_ROW_MAPPER);
    }

    public List<Lawyer> findWithFilters(String specialization, Double minRating,
                                        Double maxFee, String language, Boolean available) {
        StringBuilder sql = new StringBuilder("SELECT * FROM lawyers WHERE 1=1");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (specialization != null && !specialization.isBlank() && !specialization.equals("All")) {
            sql.append(" AND specialization = :specialization");
            params.addValue("specialization", specialization);
        }
        if (minRating != null) {
            sql.append(" AND rating >= :minRating");
            params.addValue("minRating", minRating);
        }
        if (maxFee != null) {
            sql.append(" AND consultation_fee <= :maxFee");
            params.addValue("maxFee", maxFee);
        }
        if (language != null && !language.isBlank() && !language.equals("All")) {
            sql.append(" AND FIND_IN_SET(:language, languages) > 0");
            params.addValue("language", language);
        }
        if (available != null && available) {
            sql.append(" AND available = true");
        }
        sql.append(" ORDER BY rating DESC");

        return namedJdbc.query(sql.toString(), params, LAWYER_ROW_MAPPER);
    }

    public List<Lawyer> searchByName(String query) {
        String sql = """
                SELECT * FROM lawyers
                WHERE name LIKE :query
                   OR specialization LIKE :query
                   OR location LIKE :query
                   OR bio LIKE :query
                ORDER BY rating DESC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("query", "%" + query + "%");
        return namedJdbc.query(sql, params, LAWYER_ROW_MAPPER);
    }

    public int save(Lawyer lawyer) {
        String sql = """
                INSERT INTO lawyers (id, name, avatar, specialization, experience,
                    rating, review_count, location, languages, consultation_fee,
                    bio, available, badge)
                VALUES (:id, :name, :avatar, :specialization, :experience,
                    :rating, :reviewCount, :location, :languages, :consultationFee,
                    :bio, :available, :badge)
                """;
        return namedJdbc.update(sql, buildParams(lawyer));
    }

    public int update(Lawyer lawyer) {
        String sql = """
                UPDATE lawyers
                SET name = :name, avatar = :avatar, specialization = :specialization,
                    experience = :experience, rating = :rating, review_count = :reviewCount,
                    location = :location, languages = :languages,
                    consultation_fee = :consultationFee, bio = :bio,
                    available = :available, badge = :badge
                WHERE id = :id
                """;
        return namedJdbc.update(sql, buildParams(lawyer));
    }

    public int updateAvailability(String id, boolean available) {
        String sql = "UPDATE lawyers SET available = :available WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("available", available)
                .addValue("id", id);
        return namedJdbc.update(sql, params);
    }

    public int updateRating(String id, double rating, int reviewCount) {
        String sql = "UPDATE lawyers SET rating = :rating, review_count = :reviewCount WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("rating", rating)
                .addValue("reviewCount", reviewCount)
                .addValue("id", id);
        return namedJdbc.update(sql, params);
    }

    public int deleteById(String id) {
        String sql = "DELETE FROM lawyers WHERE id = :id";
        return namedJdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    // ==========================================
    // Helper
    // ==========================================
    private MapSqlParameterSource buildParams(Lawyer l) {
        return new MapSqlParameterSource()
                .addValue("id", l.getId())
                .addValue("name", l.getName())
                .addValue("avatar", l.getAvatar())
                .addValue("specialization", l.getSpecialization())
                .addValue("experience", l.getExperience())
                .addValue("rating", l.getRating())
                .addValue("reviewCount", l.getReviewCount())
                .addValue("location", l.getLocation())
                .addValue("languages", l.getLanguages() != null ? String.join(",", l.getLanguages()) : "")
                .addValue("consultationFee", l.getConsultationFee())
                .addValue("bio", l.getBio())
                .addValue("available", l.isAvailable())
                .addValue("badge", l.getBadge());
    }
}
