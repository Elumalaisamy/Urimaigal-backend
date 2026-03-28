package com.urimaigal.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Lawyer {

    private String id;
    private String name;
    private String avatar;
    private String specialization;
    private int experience;
    private double rating;
    private int reviewCount;
    private String location;
    private List<String> languages;
    private BigDecimal consultationFee;
    private String bio;
    private boolean available;
    private String badge;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Lawyer() {}

    public Lawyer(String id, String name, String avatar, String specialization,
                  int experience, double rating, int reviewCount, String location,
                  List<String> languages, BigDecimal consultationFee,
                  String bio, boolean available, String badge,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.specialization = specialization;
        this.experience = experience;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.location = location;
        this.languages = languages;
        this.consultationFee = consultationFee;
        this.bio = bio;
        this.available = available;
        this.badge = badge;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = consultationFee; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
