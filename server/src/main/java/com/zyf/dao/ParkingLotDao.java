package com.zyf.dao;

import com.zyf.model.ParkingLot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingLotDao {
    
    // 插入停车场
    public boolean insertParkingLot(ParkingLot parkingLot) {
        String sql = "INSERT INTO parking_lot (name, address, total_spaces, available_spaces, rate1, rate2) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setString(1, parkingLot.getName());
            statement.setString(2, parkingLot.getAddress());
            statement.setInt(3, parkingLot.getTotalSpaces());
            statement.setInt(4, parkingLot.getTotalSpaces()); // 初始可用车位等于总车位
            statement.setDouble(5, parkingLot.getRate1());
            statement.setDouble(6, parkingLot.getRate2());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        parkingLot.setParkingLotId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 更新停车场费率
    public boolean updateParkingLotRates(int parkingLotId, double rate1, double rate2) {
        String sql = "UPDATE parking_lot SET rate1 = ?, rate2 = ?, updated_at = CURRENT_TIMESTAMP WHERE parking_lot_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setDouble(1, rate1);
            statement.setDouble(2, rate2);
            statement.setInt(3, parkingLotId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 更新停车场可用车位数
    public boolean updateAvailableSpaces(int parkingLotId, int availableSpaces) {
        String sql = "UPDATE parking_lot SET available_spaces = ?, updated_at = CURRENT_TIMESTAMP WHERE parking_lot_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, availableSpaces);
            statement.setInt(2, parkingLotId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 根据ID查询停车场
    public ParkingLot getParkingLotById(int parkingLotId) {
        String sql = "SELECT * FROM parking_lot WHERE parking_lot_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, parkingLotId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToParkingLot(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 获取所有停车场
    public List<ParkingLot> getAllParkingLots() {
        List<ParkingLot> parkingLots = new ArrayList<>();
        String sql = "SELECT * FROM parking_lot";
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                parkingLots.add(mapResultSetToParkingLot(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parkingLots;
    }
    
    // 将结果集映射到停车场对象
    private ParkingLot mapResultSetToParkingLot(ResultSet resultSet) throws SQLException {
        ParkingLot parkingLot = new ParkingLot();
        parkingLot.setParkingLotId(resultSet.getInt("parking_lot_id"));
        parkingLot.setName(resultSet.getString("name"));
        parkingLot.setAddress(resultSet.getString("address"));
        parkingLot.setTotalSpaces(resultSet.getInt("total_spaces"));
        parkingLot.setAvailableSpaces(resultSet.getInt("available_spaces"));
        parkingLot.setRate1(resultSet.getDouble("rate1"));
        parkingLot.setRate2(resultSet.getDouble("rate2"));
        parkingLot.setCreatedAt(resultSet.getTimestamp("created_at"));
        parkingLot.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return parkingLot;
    }

    public boolean updateParkingLot(ParkingLot parkingLot) {
        String sql = "UPDATE parking_lot SET name = ?, address = ?, total_spaces = ?, " +
                     "rate1 = ?, rate2 = ?, updated_at = CURRENT_TIMESTAMP " +
                     "WHERE parking_lot_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, parkingLot.getName());
            statement.setString(2, parkingLot.getAddress());
            statement.setInt(3, parkingLot.getTotalSpaces());
            statement.setDouble(4, parkingLot.getRate1());
            statement.setDouble(5, parkingLot.getRate2());
            statement.setInt(6, parkingLot.getParkingLotId());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 