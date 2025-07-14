package com.zyf.dao;

import com.zyf.model.ParkingRecord;
import com.zyf.model.Reservation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {

    public static void main(String[] args) {
        ParkingRecordDao parkingRecordDao = new ParkingRecordDao();
        ParkingRecord parkingRecord = new ParkingRecord(1,1,
                new Date(System.currentTimeMillis()),new Date(System.currentTimeMillis()),0.0);
        parkingRecordDao.insertParkingRecord(parkingRecord);
    }
    // 插入预约记录
    public boolean insertReservation(Reservation reservation) {
        String sql = "INSERT INTO reservation (user_id, space_id, reservation_time, start_time, end_time, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, reservation.getUserId());
            statement.setInt(2, reservation.getSpaceId());
            statement.setTimestamp(3, new Timestamp(reservation.getReservationTime().getTime()));
            statement.setTimestamp(4, null);
            statement.setTimestamp(5, null);
            statement.setInt(6, reservation.getStatus());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reservation.setReservationId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 更新预约状态
    public boolean updateReservationStatus(int reservationId, int status) {
        String sql = "UPDATE reservation SET status = ? WHERE reservation_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, status);
            statement.setInt(2, reservationId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * 根据用户ID删除所有相关预约记录
     */
    public boolean deleteReservationsByUserId(int userId) {
        String sql = "DELETE FROM reservation WHERE user_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            // 执行删除操作并检查受影响的行数
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 获取用户的所有预约
    public List<Reservation> getUserReservations(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE user_id = ? ORDER BY reservation_time DESC";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(mapResultSetToReservation(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
    
    // 获取停车位的所有预约
    public List<Reservation> getSpaceReservations(int spaceId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservation WHERE space_id = ? AND status IN (0, 1) ORDER BY start_time";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, spaceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(mapResultSetToReservation(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
    
    // 检查时间段内是否有预约
    public boolean hasOverlappingReservation(int spaceId, Timestamp startTime, Timestamp endTime) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE space_id = ? AND status IN (0, 1) " +
                    "AND ((start_time BETWEEN ? AND ?) OR (end_time BETWEEN ? AND ?))";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, spaceId);
            statement.setTimestamp(2, startTime);
            statement.setTimestamp(3, endTime);
            statement.setTimestamp(4, startTime);
            statement.setTimestamp(5, endTime);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 将结果集映射到预约对象
    private Reservation mapResultSetToReservation(ResultSet resultSet) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setReservationId(resultSet.getInt("reservation_id"));
        reservation.setUserId(resultSet.getInt("user_id"));
        reservation.setSpaceId(resultSet.getInt("space_id"));
        reservation.setReservationTime(resultSet.getTimestamp("reservation_time"));
        reservation.setStartTime(resultSet.getTimestamp("start_time"));
        reservation.setEndTime(resultSet.getTimestamp("end_time"));
        reservation.setStatus(resultSet.getInt("status"));
        return reservation;
    }

    // 更新预约的开始时间
    public boolean updateStartTimeByUserIdAndSpaceId(int userId, int spaceId, Date startTime) {
        String sql = "UPDATE reservation SET start_time = ? WHERE user_id = ? AND space_id = ? " +
                    "ORDER BY reservation_time DESC LIMIT 1";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setTimestamp(1, new Timestamp(startTime.getTime()));
            statement.setInt(2, userId);
            statement.setInt(3, spaceId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
} 