package com.zyf.dao;

import com.zyf.model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {
    
    // 插入支付记录
    public boolean insertPayment(Payment payment) {
        String sql = "INSERT INTO payment (user_id, amount, payment_method, payment_time, order_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, payment.getUserId());
            statement.setDouble(2, payment.getAmount());
            statement.setString(3, payment.getPaymentMethod());
            statement.setTimestamp(4, new Timestamp(payment.getPaymentTime().getTime()));
            statement.setInt(5, payment.getOrderId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setPaymentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 获取用户的所有支付记录
    public List<Payment> getUserPayments(int userId) {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE user_id = ? ORDER BY payment_time DESC";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(mapResultSetToPayment(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }
    
    // 获取指定订单的支付记录
    public Payment getPaymentByOrderId(int orderId) {
        String sql = "SELECT * FROM payment WHERE order_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToPayment(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 将结果集映射到支付记录对象
    private Payment mapResultSetToPayment(ResultSet resultSet) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(resultSet.getInt("payment_id"));
        payment.setUserId(resultSet.getInt("user_id"));
        payment.setAmount(resultSet.getDouble("amount"));
        payment.setPaymentMethod(resultSet.getString("payment_method"));
        payment.setPaymentTime(resultSet.getTimestamp("payment_time"));
        payment.setOrderId(resultSet.getInt("order_id"));
        return payment;
    }
} 