package com.urimaigal.repository;

import com.urimaigal.model.Consultation;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ConsultationRepository {

    private final NamedParameterJdbcTemplate namedJdbc;

    public ConsultationRepository(NamedParameterJdbcTemplate namedJdbc) {
        this.namedJdbc = namedJdbc;
    }

    private static final RowMapper<Consultation> CONSULTATION_ROW_MAPPER = new RowMapper<>() {
        @Override
        public Consultation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Consultation c = new Consultation();
            c.setId(rs.getString("id"));
            c.setClientId(rs.getString("client_id"));
            c.setAdvocateId(rs.getString("advocate_id"));
            c.setLawyerName(rs.getString("lawyer_name"));
            c.setConsultationDate(rs.getDate("consultation_date") != null
                    ? rs.getDate("consultation_date").toLocalDate() : null);
            c.setConsultationTime(rs.getString("consultation_time"));
            c.setMode(rs.getString("mode"));
            c.setStatus(rs.getString("status"));
            c.setFee(rs.getBigDecimal("fee"));
            c.setNotes(rs.getString("notes"));
            c.setCreatedAt(rs.getTimestamp("created_at") != null
                    ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            c.setUpdatedAt(rs.getTimestamp("updated_at") != null
                    ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return c;
        }
    };

    public List<Consultation> findByClientId(String clientId) {
        String sql = """
                SELECT * FROM consultations
                WHERE client_id = :clientId
                ORDER BY consultation_date DESC, consultation_time DESC
                """;
        return namedJdbc.query(sql, new MapSqlParameterSource("clientId", clientId), CONSULTATION_ROW_MAPPER);
    }

    public List<Consultation> findByAdvocateId(String advocateId) {
        String sql = """
                SELECT * FROM consultations
                WHERE advocate_id = :advocateId
                ORDER BY consultation_date DESC, consultation_time DESC
                """;
        return namedJdbc.query(sql, new MapSqlParameterSource("advocateId", advocateId), CONSULTATION_ROW_MAPPER);
    }

    public List<Consultation> findByAdvocateIdAndStatus(String advocateId, String status) {
        String sql = """
                SELECT * FROM consultations
                WHERE advocate_id = :advocateId AND status = :status
                ORDER BY consultation_date DESC, consultation_time DESC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("advocateId", advocateId)
                .addValue("status", status);
        return namedJdbc.query(sql, params, CONSULTATION_ROW_MAPPER);
    }

    public Optional<Consultation> findById(String id) {
        String sql = "SELECT * FROM consultations WHERE id = :id";
        List<Consultation> results = namedJdbc.query(sql, new MapSqlParameterSource("id", id), CONSULTATION_ROW_MAPPER);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public int save(Consultation consultation) {
        String sql = """
                INSERT INTO consultations (id, client_id, advocate_id, lawyer_name,
                    consultation_date, consultation_time, mode, status, fee, notes)
                VALUES (:id, :clientId, :advocateId, :lawyerName,
                    :consultationDate, :consultationTime, :mode, :status, :fee, :notes)
                """;
        return namedJdbc.update(sql, buildParams(consultation));
    }

    public int updateStatus(String id, String status) {
        String sql = "UPDATE consultations SET status = :status WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("id", id);
        return namedJdbc.update(sql, params);
    }

    private MapSqlParameterSource buildParams(Consultation c) {
        return new MapSqlParameterSource()
                .addValue("id", c.getId())
                .addValue("clientId", c.getClientId())
                .addValue("advocateId", c.getAdvocateId())
                .addValue("lawyerName", c.getLawyerName())
                .addValue("consultationDate", c.getConsultationDate())
                .addValue("consultationTime", c.getConsultationTime())
                .addValue("mode", c.getMode())
                .addValue("status", c.getStatus())
                .addValue("fee", c.getFee())
                .addValue("notes", c.getNotes());
    }
}
