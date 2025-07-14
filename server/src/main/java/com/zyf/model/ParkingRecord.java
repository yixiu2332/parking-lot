package com.zyf.model;

import java.util.Date;

public class ParkingRecord {
    private Integer recordId;
    private Integer userId;
    private Integer spaceId;
    private Date entryTime;
    private Date exitTime;
    private Double cost;
    private Integer paid;

    public ParkingRecord() {
    }

    public ParkingRecord(Integer userId, Integer spaceId, Date entryTime, Date exitTime, Double cost) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.cost = cost;
    }

    // Getters and Setters
    public Integer getRecordId() {
        return recordId;
    }
    
    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public Integer getSpaceId() {
        return spaceId;
    }
    
    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }
    
    public Date getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }
    
    public Date getExitTime() {
        return exitTime;
    }
    
    public void setExitTime(Date exitTime) {
        this.exitTime = exitTime;
    }
    
    public Double getCost() {
        return cost;
    }
    
    public void setCost(Double cost) {
        this.cost = cost;
    }
    
    public Integer getPaid() {
        return paid;
    }
    
    public void setPaid(Integer paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return "ParkingRecord{" +
                "recordId=" + recordId +
                ", userId=" + userId +
                ", spaceId=" + spaceId +
                ", entryTime=" + entryTime +
                ", exitTime=" + exitTime +
                ", cost=" + cost +
                ", paid=" + paid +
                '}';
    }
}