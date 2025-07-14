package com.zyf.model;

import java.util.Date;

public class Reservation {
    private Integer reservationId;
    private Integer userId;
    private Integer spaceId;
    private Date reservationTime;
    private Date startTime;
    private Date endTime;
    private Integer status;

    public Reservation() {

    }

    public Reservation(Integer userId, Integer spaceId, Date reservationTime, Integer status) {
        this.userId = userId;
        this.spaceId = spaceId;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    // Getters and Setters
    public Integer getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
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
    
    public Date getReservationTime() {
        return reservationTime;
    }
    
    public void setReservationTime(Date reservationTime) {
        this.reservationTime = reservationTime;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
} 