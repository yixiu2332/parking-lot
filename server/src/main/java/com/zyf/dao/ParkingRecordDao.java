package com.zyf.dao;

import com.zyf.model.ParkingRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParkingRecordDao {
    
    // 插入停车记录
    public boolean insertParkingRecord(ParkingRecord record) {
        String sql = "INSERT INTO parking_record (user_id, space_id, entry_time, exit_time, cost, paid) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, record.getUserId());
            statement.setInt(2, record.getSpaceId());
            statement.setTimestamp(3, new Timestamp(record.getEntryTime().getTime()));
            statement.setTimestamp(4, new Timestamp(record.getExitTime().getTime()));
            statement.setDouble(5, record.getCost());
            statement.setInt(6, record.getCost()<=1 ? 1 : 0);
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setRecordId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 根据userId查询第一个paid为0的订单
    public ParkingRecord getFirstUnpaidRecordByUserId(int userId) {
        String sql = "SELECT * FROM parking_record WHERE user_id = ? AND paid = 0 ORDER BY entry_time ASC LIMIT 1";

        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToParkingRecord(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 如果没有找到符合条件的记录，则返回null
    }

    public boolean markFirstUnpaidRecordAsPaidByUserId(int userId) {
        // Step 1: 查找第一个未支付的停车记录
        String selectSql = "SELECT * FROM parking_record WHERE user_id = ? AND paid = 0 ORDER BY entry_time ASC LIMIT 1";

        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {

            selectStatement.setInt(1, userId);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    // 将结果集映射到ParkingRecord对象
                    ParkingRecord record = mapResultSetToParkingRecord(resultSet);

                    // Step 2: 更新这条记录的paid状态为1
                    String updateSql = "UPDATE parking_record SET paid = 1 WHERE record_id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                        updateStatement.setInt(1, record.getRecordId());

                        return updateStatement.executeUpdate() > 0; // 返回是否成功更新
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // 如果没有找到符合条件的记录或更新失败，则返回false
    }

    // 更新停车记录（离场时间和费用）
    public boolean updateParkingRecord(ParkingRecord record) {
        String sql = "UPDATE parking_record SET exit_time = ?, cost = ?, paid = ? WHERE record_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setTimestamp(1, new Timestamp(record.getExitTime().getTime()));
            statement.setDouble(2, record.getCost());
            statement.setInt(3, record.getPaid());
            statement.setInt(4, record.getRecordId());
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 通过userId和spaceId设置最后一条记录的退出时间
    public boolean setExitTimeOfLastRecordByUserIdAndSpaceId(int userId, int spaceId, Timestamp exitTime) {
        // 查询最后一条记录的record_id
        String selectSql = "SELECT record_id FROM parking_record WHERE user_id = ? AND space_id = ? ORDER BY entry_time DESC LIMIT 1";

        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {

            selectStatement.setInt(1, userId);
            selectStatement.setInt(2, spaceId);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    int recordId = resultSet.getInt("record_id");

                    // 更新指定record_id的记录的exit_time
                    String updateSql = "UPDATE parking_record SET exit_time = ? WHERE record_id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                        updateStatement.setTimestamp(1, exitTime);
                        updateStatement.setInt(2, recordId);

                        return updateStatement.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 获取用户的所有停车记录
    public List<ParkingRecord> getUserParkingRecords(int userId) {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_record WHERE user_id = ? ORDER BY entry_time DESC";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToParkingRecord(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    // 获取未支付的停车记录
    public List<ParkingRecord> getUnpaidRecords() {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_record WHERE paid = 0";
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                records.add(mapResultSetToParkingRecord(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    // 获取所有停车记录
    public List<ParkingRecord> getAllParkingRecords() {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_record ORDER BY entry_time DESC";
        
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                records.add(mapResultSetToParkingRecord(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    // 获取已支付的停车记录
    public List<ParkingRecord> getPaidRecordsSince(java.util.Date date) {
        List<ParkingRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM parking_record WHERE paid = 1 AND entry_time >= ? ORDER BY entry_time DESC";
        
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // 将Java的Date转换为SQL的Timestamp
            java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(date.getTime());
            statement.setTimestamp(1, sqlTimestamp);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToParkingRecord(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
    
    // 将结果集映射到停车记录对象
    private ParkingRecord mapResultSetToParkingRecord(ResultSet resultSet) throws SQLException {
        ParkingRecord record = new ParkingRecord();
        record.setRecordId(resultSet.getInt("record_id"));
        record.setUserId(resultSet.getInt("user_id"));
        record.setSpaceId(resultSet.getInt("space_id"));
        record.setEntryTime(resultSet.getTimestamp("entry_time"));
        record.setExitTime(resultSet.getTimestamp("exit_time"));
        record.setCost(resultSet.getDouble("cost"));
        record.setPaid(resultSet.getInt("paid"));
        return record;
    }

    public static void main(String[] args) {
        ParkingRecordDao parkingRecordDao = new ParkingRecordDao();
        List<ParkingRecord> userParkingRecords = parkingRecordDao.getUserParkingRecords(1);
        for (ParkingRecord record : userParkingRecords) {
            System.out.println(record);
        }
    }
} 