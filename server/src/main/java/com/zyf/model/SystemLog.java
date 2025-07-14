package com.zyf.model;

import java.util.Date;

public class SystemLog {
    private Integer logId;
    private Integer adminId;
    private String action;
    private Date timestamp;
    
    // Getters and Setters
    public Integer getLogId() {
        return logId;
    }
    
    public void setLogId(Integer logId) {
        this.logId = logId;
    }
    
    public Integer getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 