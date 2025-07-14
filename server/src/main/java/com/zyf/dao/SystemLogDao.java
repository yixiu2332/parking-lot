package com.zyf.dao;

import com.zyf.model.SystemLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemLogDao {
    
    // 插入系统日志
    public boolean insertLog(SystemLog log) {
        String sql = "INSERT INTO system_log (admin_id, action, timestamp) VALUES (?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (log.getAdminId() != null) {
                statement.setInt(1, log.getAdminId());
            } else {
                statement.setNull(1, Types.INTEGER);
            }
            statement.setString(2, log.getAction());
            statement.setTimestamp(3, new Timestamp(log.getTimestamp().getTime()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setLogId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 获取管理员的操作日志
    public List<SystemLog> getAdminLogs(int adminId) {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_log WHERE admin_id = ? ORDER BY timestamp DESC";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, adminId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToSystemLog(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    // 获取最近的系统日志
    public List<SystemLog> getRecentLogs(int limit) {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_log ORDER BY timestamp DESC LIMIT ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(mapResultSetToSystemLog(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    // 将结果集映射到系统日志对象
    private SystemLog mapResultSetToSystemLog(ResultSet resultSet) throws SQLException {
        SystemLog log = new SystemLog();
        log.setLogId(resultSet.getInt("log_id"));
        
        int adminId = resultSet.getInt("admin_id");
        if (!resultSet.wasNull()) {
            log.setAdminId(adminId);
        }
        
        log.setAction(resultSet.getString("action"));
        log.setTimestamp(resultSet.getTimestamp("timestamp"));
        return log;
    }
} 