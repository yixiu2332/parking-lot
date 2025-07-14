package com.zyf.model;

import java.util.Arrays;
import java.util.Date;

public class User {
    private int userId; // 用户ID
    private String name; // 用户名
    private String account; // 账号
    private String password; // 密码
    private String phone; // 联系电话
    private byte[] image; // 用户头像
    private String licensePlate; // 车牌号
    private double balance; // 账户余额
    private Date createdAt; // 注册时间
    private Date updatedAt;

    // Getter 和 Setter 方法


    public User(String name, String account, String password, String phone) {
        this.name = name;
        this.account = account;
        this.password = password;
        this.phone = phone;
        this.image = null;
        this.licensePlate = null;
        this.balance = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    public User(){}

    public User(int userId, String name, byte[] image) {
        this.userId = userId;
        this.name = name;
        this.image = image;

    }

    public int getUserId() {
        return userId;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    // 可选：重写 toString 方法以便于调试和日志记录

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", phone='" + phone + '\'' +
                ", image=" + Arrays.toString(image) +
                ", licensePlate='" + licensePlate + '\'' +
                ", balance=" + balance +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}