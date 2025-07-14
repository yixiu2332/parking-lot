package com.zyf.dao;

import com.zyf.model.ParkingSpace;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpaceDao {
    
    // 获取最小的未使用ID
    private int getNextAvailableId() {
        // 先查找最大的ID
        String sql = "SELECT COALESCE(MAX(space_id), 0) AS max_id FROM parking_space";
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            if (resultSet.next()) {
                int maxId = resultSet.getInt("max_id");
                
                // 如果表为空，返回1作为第一个ID
                if (maxId == 0) {
                    return 1;
                }
                
                // 查找空缺的ID
                sql = "SELECT t1.space_id + 1 AS next_id " +
                      "FROM parking_space t1 " +
                      "LEFT JOIN parking_space t2 ON t1.space_id + 1 = t2.space_id " +
                      "WHERE t2.space_id IS NULL AND t1.space_id < " + maxId + " " +
                      "ORDER BY t1.space_id " +
                      "LIMIT 1";
                
                try (ResultSet rs = statement.executeQuery(sql)) {
                    if (rs.next()) {
                        return rs.getInt("next_id");
                    } else {
                        // 如果没有空缺的ID，返回最大ID + 1
                        return maxId + 1;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // 如果发生错误，返回1
    }
    
    // 插入停车位
    public boolean insertParkingSpace(ParkingSpace space) {
        String sql = "INSERT INTO parking_space (space_id, parking_lot_id, status, space_number, lock_status, reserved_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            int nextId = getNextAvailableId();
            
            statement.setInt(1, nextId);
            statement.setInt(2, space.getParkingLotId());
            statement.setInt(3, space.getStatus());
            statement.setString(4, space.getSpaceNumber());
            statement.setInt(5, space.getLockStatus());
            statement.setString(6, space.getReservedBy());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                space.setSpaceId(nextId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 更新停车位状态
    public boolean updateSpaceStatus(int spaceId, int status) {
        String sql = "UPDATE parking_space SET status = ? WHERE space_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, status);
            statement.setInt(2, spaceId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 更新车位锁状态
    public boolean updateLockStatus(int spaceId, int lockStatus) {
        String sql = "UPDATE parking_space SET lock_status = ? WHERE space_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, lockStatus);
            statement.setInt(2, spaceId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 更新预约用户
    public boolean updateReservedBy(int spaceId, String account) {
        String sql = "UPDATE parking_space SET reserved_by = ? WHERE space_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, account);
            statement.setInt(2, spaceId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 根据ID查询停车位
    public ParkingSpace getParkingSpaceById(int spaceId) {
        String sql = "SELECT * FROM parking_space WHERE space_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, spaceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToParkingSpace(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 获取指定停车场的所有停车位
    public List<ParkingSpace> getParkingSpacesByLotId(int parkingLotId) {
        List<ParkingSpace> spaces = new ArrayList<>();
        String sql = "SELECT * FROM parking_space WHERE parking_lot_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, parkingLotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    spaces.add(mapResultSetToParkingSpace(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spaces;
    }
    
    // 获取所有空闲停车位
    public List<ParkingSpace> getAvailableSpaces() {
        List<ParkingSpace> spaces = new ArrayList<>();
        String sql = "SELECT * FROM parking_space WHERE status = 0"; // 0:可用, 1:使用中, 2:停用
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                spaces.add(mapResultSetToParkingSpace(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spaces;
    }
    
    // 获取所有停车位
    public List<ParkingSpace> getAllParkingSpaces() {
        List<ParkingSpace> spaces = new ArrayList<>();
        String sql = "SELECT * FROM parking_space ORDER BY parking_lot_id, space_number";
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                spaces.add(mapResultSetToParkingSpace(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spaces;
    }
    
    // 将结果集映射到停车位对象
    private ParkingSpace mapResultSetToParkingSpace(ResultSet resultSet) throws SQLException {
        ParkingSpace space = new ParkingSpace();
        space.setSpaceId(resultSet.getInt("space_id"));
        space.setParkingLotId(resultSet.getInt("parking_lot_id"));
        space.setStatus(resultSet.getInt("status"));
        space.setSpaceNumber(resultSet.getString("space_number"));
        space.setLockStatus(resultSet.getInt("lock_status"));
        space.setReservedBy(resultSet.getString("reserved_by"));
        space.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return space;
    }
} 