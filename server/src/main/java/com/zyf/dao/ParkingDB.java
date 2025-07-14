package com.zyf.dao;

import com.zyf.model.User;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import java.sql.Connection;
import java.sql.SQLException;

public class ParkingDB {

    private static final DataSource dataSource;

    static {
        // 配置连接池属性
        PoolProperties p = new PoolProperties();
        p.setUrl("jdbc:mysql://localhost:3306/parking");
        p.setDriverClassName("com.mysql.cj.jdbc.Driver");
        p.setUsername("root");
        // 从环境变量中读取密码
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = "123qwe";
        }
        p.setPassword(password);
        
        // 连接池基本配置
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        
        // 连接池大小配置
        p.setMaxActive(20);
        p.setInitialSize(5);
        p.setMaxWait(10000);
        p.setMinIdle(5);
        p.setMaxIdle(10);
        
        // 连接池安全配置
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        
        // 配置拦截器
        p.setJdbcInterceptors(
            "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
            "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer"
        );

        // 创建数据源
        dataSource = new DataSource();
        dataSource.setPoolProperties(p);
    }

    // 获取数据库连接
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // 关闭数据源
    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close(true);
        }
    }

    // 测试连接
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 测试方法
    public static void main(String[] args) {
        if (testConnection()) {
            System.out.println("数据库连接池测试成功");
            UserDao userDao = new UserDao();
            User userByAccount = userDao.getUserByAccount("1");
            System.out.println(userByAccount);
        } else {
            System.out.println("数据库连接池测试失败");
        }
    }
}