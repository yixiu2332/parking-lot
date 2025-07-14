package com.zyf.partinglot.dao;

public class User {
    private int userId;
    private String name;



    private String account;

    private String password;
    private String phone;

    private String licensePlate;
    private byte[] image;

    public User(String name, String account, String password, String phone, String licensePlate, byte[] image) {
        this.name = name;
        this.account = account;
        this.password = password;
        this.phone = phone;
        this.licensePlate = licensePlate;
        this.image = image;
    }

    public User() {

    }

    public User(String name, String account, String password, String phone) {
        this.name = name;
        this.account = account;
        this.password = password;
        this.phone = phone;
        this.licensePlate = null;
        this.image = null;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    @Override
    public String toString() {
        return "User{" +
                "account='" + account + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
