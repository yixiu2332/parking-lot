package com.zyf.model;

import java.util.Date;

public class LicenseReview {
    private Integer reviewId;
    private Integer userId;
    private String licensePlate;
    private Integer status;
    private Date submittedAt;
    private Date reviewedAt;

    public LicenseReview() {
    }

    public LicenseReview(Integer userId, String licensePlate, Integer status, Date submittedAt) {
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    // Getters and Setters
    public Integer getReviewId() {
        return reviewId;
    }
    
    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Date getSubmittedAt() {
        return submittedAt;
    }
    
    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public Date getReviewedAt() {
        return reviewedAt;
    }
    
    public void setReviewedAt(Date reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
} 