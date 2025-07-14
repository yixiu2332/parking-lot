package com.zyf.dao;

import com.zyf.model.LicenseReview;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LicenseReviewDao {
    
    // 插入车牌审核记录
    public boolean insertLicenseReview(LicenseReview review) {
        String sql = "INSERT INTO license_review (user_id, license_plate, status, submitted_at) " +
                    "VALUES (?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            statement.setInt(1, review.getUserId());
            statement.setString(2, review.getLicensePlate());
            statement.setInt(3, review.getStatus());
            statement.setTimestamp(4, new Timestamp(review.getSubmittedAt().getTime()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        review.setReviewId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 更新审核状态
    public boolean updateReviewStatus(int reviewId, int status) {
        String sql = "UPDATE license_review SET status = ?, reviewed_at = CURRENT_TIMESTAMP WHERE review_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, status);
            statement.setInt(2, reviewId);
            
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 获取待审核的记录
    public List<LicenseReview> getPendingReviews() {
        List<LicenseReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM license_review WHERE status = 0 ORDER BY submitted_at";
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                reviews.add(mapResultSetToLicenseReview(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }
    
    // 获取用户的审核记录
    public List<LicenseReview> getUserReviews(int userId) {
        List<LicenseReview> reviews = new ArrayList<>();
        String sql = "SELECT * FROM license_review WHERE user_id = ? ORDER BY submitted_at DESC";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reviews.add(mapResultSetToLicenseReview(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reviews;
    }
    
    // 检查车牌号是否正在审核中
    public boolean isLicensePlateUnderReview(String licensePlate) {
        String sql = "SELECT COUNT(*) FROM license_review WHERE license_plate = ? AND status = 0";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, licensePlate);
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
    
    // 根据ID获取审核记录
    public LicenseReview getReviewById(int reviewId) {
        String sql = "SELECT * FROM license_review WHERE review_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, reviewId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToLicenseReview(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 将结果集映射到车牌审核对象
    private LicenseReview mapResultSetToLicenseReview(ResultSet resultSet) throws SQLException {
        LicenseReview review = new LicenseReview();
        review.setReviewId(resultSet.getInt("review_id"));
        review.setUserId(resultSet.getInt("user_id"));
        review.setLicensePlate(resultSet.getString("license_plate"));
        review.setStatus(resultSet.getInt("status"));
        review.setSubmittedAt(resultSet.getTimestamp("submitted_at"));
        review.setReviewedAt(resultSet.getTimestamp("reviewed_at"));
        return review;
    }
} 