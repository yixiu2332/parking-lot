package com.zyf.model;

import java.util.Date;

public class ParkingLot {
    private Integer parkingLotId;
    private String name;
    private String address;
    private Integer totalSpaces;
    private Integer availableSpaces;
    private Double rate1;
    private Double rate2;
    private Date createdAt;
    private Date updatedAt;
    
    // Getters and Setters
    public Integer getParkingLotId() {
        return parkingLotId;
    }
    
    public void setParkingLotId(Integer parkingLotId) {
        this.parkingLotId = parkingLotId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public Integer getTotalSpaces() {
        return totalSpaces;
    }
    
    public void setTotalSpaces(Integer totalSpaces) {
        this.totalSpaces = totalSpaces;
    }
    
    public Integer getAvailableSpaces() {
        return availableSpaces;
    }
    
    public void setAvailableSpaces(Integer availableSpaces) {
        this.availableSpaces = availableSpaces;
    }
    
    public Double getRate1() {
        return rate1;
    }
    
    public void setRate1(Double rate1) {
        this.rate1 = rate1;
    }
    
    public Double getRate2() {
        return rate2;
    }
    
    public void setRate2(Double rate2) {
        this.rate2 = rate2;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 