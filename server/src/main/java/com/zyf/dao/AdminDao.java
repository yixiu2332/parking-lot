package com.zyf.dao;

import com.zyf.model.Admin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDao {
    
    // 插入管理员
    public boolean insertAdmin(Admin admin) {
        String sql = "INSERT INTO admin (username, password, phone, created_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, admin.getUsername());
            statement.setString(2, admin.getPassword());
            statement.setString(3, admin.getPhone());
            statement.setTimestamp(4, new Timestamp(admin.getCreatedAt().getTime()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        admin.setAdminId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 根据ID查询管理员
    public Admin getAdminById(int adminId) {
        String sql = "SELECT * FROM admin WHERE admin_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, adminId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToAdmin(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 根据用户名查询管理员
    public Admin getAdminByUsername(String username) {
        String sql = "SELECT * FROM admin WHERE username = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToAdmin(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 更新管理员信息
    public boolean updateAdmin(Admin admin) {
        String sql = "UPDATE admin SET username = ?, password = ?, phone = ? WHERE admin_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, admin.getUsername());
            statement.setString(2, admin.getPassword());
            statement.setString(3, admin.getPhone());
            statement.setInt(4, admin.getAdminId());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 删除管理员
    public boolean deleteAdmin(int adminId) {
        String sql = "DELETE FROM admin WHERE admin_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, adminId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 获取所有管理员
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM admin";
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                admins.add(mapResultSetToAdmin(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return admins;
    }
    
    // 将结果集映射到管理员对象
    private Admin mapResultSetToAdmin(ResultSet resultSet) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(resultSet.getInt("admin_id"));
        admin.setUsername(resultSet.getString("username"));
        admin.setPassword(resultSet.getString("password"));
        admin.setPhone(resultSet.getString("phone"));
        admin.setCreatedAt(resultSet.getTimestamp("created_at"));
        return admin;
    }
} 