package com.zyf.dao;

import com.zyf.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.Random;

public class UserDao {
    public static void main(String[] args) {
        UserDao userDao = new UserDao();
        User user = new User("zyf","zyf","zyf","zyf");
        userDao.insertUser(user);
    }



    public byte[] getDefaultUserImage() {
        // 创建一个64x64的BufferedImage
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 生成随机颜色
        Random random = new Random();
        Color randomColor = new Color(
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        );

        // 填充背景
        g2d.setColor(randomColor);
        g2d.fillRect(0, 0, 64, 64);

        // 添加一些随机图形
        for (int i = 0; i < 5; i++) {
            g2d.setColor(new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            ));
            int x = random.nextInt(44);
            int y = random.nextInt(44);
            g2d.fillOval(x, y, 20, 20);
        }

        g2d.dispose();

        // 将图片转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    // 插入用户
    public boolean insertUser(User user) {
        String sql = "INSERT INTO user (name, account, password, phone, image, license_plate, balance, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getAccount());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());
            statement.setBytes(5, getDefaultUserImage());
            statement.setString(6, null);
            statement.setDouble(7, 0);
            statement.setTimestamp(8, new Timestamp(user.getCreatedAt().getTime()));
            statement.setTimestamp(9, new Timestamp(user.getUpdatedAt().getTime()));

            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 查询用户
    public User getUserById(int userId) {
        String sql = "SELECT * FROM user WHERE user_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 查询用户通过账号
    public User getUserByAccount(String account) {
        String sql = "SELECT * FROM user WHERE account = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, account);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 查询用户通过手机号
    public User getUserByPhone(String phone) {
        String sql = "SELECT * FROM user WHERE phone = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, phone);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 更新用户
    public boolean updateUser(User user) {
        String sql = "UPDATE user SET name = ?, account = ?, password = ?, phone = ?, image = ?, license_plate = ?, balance = ?, updated_at = ? WHERE user_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getAccount());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());
            statement.setBytes(5, user.getImage());
            statement.setString(6, user.getLicensePlate());
            statement.setDouble(7, user.getBalance());
            statement.setTimestamp(8, new Timestamp(user.getUpdatedAt().getTime()));
            statement.setInt(9, user.getUserId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 更新用户
    public boolean updateUserSimple(User user) {
        String sql = "UPDATE user SET name = ?, image = ?, updated_at = ? WHERE user_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setBytes(2, user.getImage());
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            statement.setInt(4, user.getUserId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 删除用户
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM user WHERE user_id = ?";
        try (Connection connection = ParkingDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 查询所有用户
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try (Connection connection = ParkingDB.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // 将结果集映射到用户对象
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setUserId(resultSet.getInt("user_id"));
        user.setName(resultSet.getString("name"));
        user.setAccount(resultSet.getString("account"));
        user.setPassword(resultSet.getString("password"));
        user.setPhone(resultSet.getString("phone"));
        user.setImage(resultSet.getBytes("image"));
        user.setLicensePlate(resultSet.getString("license_plate"));
        user.setBalance(resultSet.getDouble("balance"));
        user.setCreatedAt(resultSet.getTimestamp("created_at"));
        user.setUpdatedAt(resultSet.getTimestamp("updated_at"));
        return user;
    }
}