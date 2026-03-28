package com.urimaigal.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class BookingEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bookingId;
    private String userId;
    private String lawyerId;
    private String lawyerName;
    private String userEmail;
    private String date;
    private String time;
    private String mode;
    private BigDecimal fee;
    private String eventType; // BOOKING_CREATED | BOOKING_CANCELLED

    public BookingEvent() {}

    public BookingEvent(String bookingId, String userId, String lawyerId, String lawyerName,
                        String userEmail, String date, String time, String mode,
                        BigDecimal fee, String eventType) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.lawyerId = lawyerId;
        this.lawyerName = lawyerName;
        this.userEmail = userEmail;
        this.date = date;
        this.time = time;
        this.mode = mode;
        this.fee = fee;
        this.eventType = eventType;
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getLawyerId() { return lawyerId; }
    public void setLawyerId(String lawyerId) { this.lawyerId = lawyerId; }
    public String getLawyerName() { return lawyerName; }
    public void setLawyerName(String lawyerName) { this.lawyerName = lawyerName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}
