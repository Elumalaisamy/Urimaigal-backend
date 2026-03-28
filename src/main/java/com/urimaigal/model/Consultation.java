package com.urimaigal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Consultation {

    private String id;
    private String clientId;
    private String advocateId;
    private String lawyerName;
    private LocalDate consultationDate;
    private String consultationTime;
    private String mode;   // chat | video
    private String status; // scheduled | completed | cancelled
    private BigDecimal fee;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Consultation() {}

    public Consultation(String id, String clientId, String advocateId, String lawyerName,
                        LocalDate consultationDate, String consultationTime, String mode,
                        String status, BigDecimal fee, String notes,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.advocateId = advocateId;
        this.lawyerName = lawyerName;
        this.consultationDate = consultationDate;
        this.consultationTime = consultationTime;
        this.mode = mode;
        this.status = status;
        this.fee = fee;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getAdvocateId() { return advocateId; }
    public void setAdvocateId(String advocateId) { this.advocateId = advocateId; }

    public String getLawyerName() { return lawyerName; }
    public void setLawyerName(String lawyerName) { this.lawyerName = lawyerName; }

    public LocalDate getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDate consultationDate) { this.consultationDate = consultationDate; }

    public String getConsultationTime() { return consultationTime; }
    public void setConsultationTime(String consultationTime) { this.consultationTime = consultationTime; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
