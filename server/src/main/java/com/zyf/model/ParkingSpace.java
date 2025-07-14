package com.zyf.model;

import java.util.Date;

public class ParkingSpace {
    private Integer spaceId;
    private Integer parkingLotId;
    private Integer status;
    private String spaceNumber;
    private Integer lockStatus;
    private String reservedBy;
    private Date updatedAt;
    
    // Getters and Setters
    public Integer getSpaceId() {
        return spaceId;
    }
    
    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }
    
    public Integer getParkingLotId() {
        return parkingLotId;
    }
    
    public void setParkingLotId(Integer parkingLotId) {
        this.parkingLotId = parkingLotId;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getSpaceNumber() {
        return spaceNumber;
    }
    
    public void setSpaceNumber(String spaceNumber) {
        this.spaceNumber = spaceNumber;
    }
    
    public Integer getLockStatus() {
        return lockStatus;
    }
    
    public void setLockStatus(Integer lockStatus) {
        this.lockStatus = lockStatus;
    }
    
    public String getReservedBy() {
        return reservedBy;
    }
    
    public void setReservedBy(String reservedBy) {
        this.reservedBy = reservedBy;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 