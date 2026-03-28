package com.urimaigal.dto;

import jakarta.validation.constraints.NotBlank;

public class BookingRequest {

    @NotBlank(message = "Lawyer ID is required")
    private String lawyerId;

    @NotBlank(message = "Date is required")
    private String date;

    @NotBlank(message = "Time is required")
    private String time;

    @NotBlank(message = "Mode is required (chat or video)")
    private String mode;

    private String notes;

    public BookingRequest() {}

    public String getLawyerId() { return lawyerId; }
    public void setLawyerId(String lawyerId) { this.lawyerId = lawyerId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
