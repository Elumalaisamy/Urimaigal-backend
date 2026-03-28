package com.urimaigal.dto;

import java.math.BigDecimal;

public class LawyerFilterRequest {

    private String category;
    private Double minRating;
    private BigDecimal maxFee;
    private String language;
    private Boolean available;
    private String query;

    public LawyerFilterRequest() {}

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }
    public BigDecimal getMaxFee() { return maxFee; }
    public void setMaxFee(BigDecimal maxFee) { this.maxFee = maxFee; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
}
