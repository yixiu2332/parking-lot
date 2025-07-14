package com.zyf.service;

import com.zyf.dao.ParkingDB;
import com.zyf.dao.ParkingLotDao;
import com.zyf.dao.ParkingSpaceDao;
import com.zyf.dao.ReservationDao;
import com.zyf.dao.UserDao;
import com.zyf.model.ParkingLot;
import com.zyf.model.ParkingSpace;
import com.zyf.model.Reservation;
import com.zyf.model.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

public class ParkingTransactionService {
    public static void main(String[] args) {
        ParkingTransactionService parkingTransactionService = new ParkingTransactionService();
        parkingTransactionService.updateSpaceStatus(1,1,0);
    }

    public int updateSpaceStatus(int userId, int spaceId, int status) {
        Connection connection = null;
        try {
            connection = ParkingDB.getConnection();
            connection.setAutoCommit(false); // 开启事务

            ParkingSpaceDao parkingSpaceDao = new ParkingSpaceDao();
            UserDao userDao = new UserDao();
            
            synchronized (ParkingLotDao.class) {
                ParkingSpace spaceById = parkingSpaceDao.getParkingSpaceById(spaceId);
                if (spaceById.getStatus() == status) {
                    System.out.println("No status change needed.");
                    return 0;
                }
                
                // 更新车位状态
                parkingSpaceDao.updateSpaceStatus(spaceId, status);
                
                // 更新预约人
                if (status == 1) {
                    // 获取用户账号
                    User user = userDao.getUserById(userId);
                    if (user != null) {
                        parkingSpaceDao.updateReservedBy(spaceId, user.getAccount());
                    }
                } else {
                    parkingSpaceDao.updateReservedBy(spaceId, null);
                }
                
                ParkingLotDao lotDao = new ParkingLotDao();
                ParkingLot parkingLotById = lotDao.getParkingLotById(1);
                Integer availableSpaces = parkingLotById.getAvailableSpaces();
                ReservationDao reservationDao = new ReservationDao();
                
                if (status == 0) {
                    reservationDao.deleteReservationsByUserId(userId);
                    availableSpaces++;
                } else {
                    Date date = new Date(System.currentTimeMillis());
                    Reservation reservation = new Reservation(userId, spaceId, date, status);
                    reservationDao.insertReservation(reservation);
                    availableSpaces--;
                }
                lotDao.updateAvailableSpaces(1, availableSpaces);
            }

            connection.commit(); // 提交事务
            return 1;
        } catch (Exception e) {
            // 出现异常时回滚事务
            System.out.println("3");
            e.printStackTrace(); // 打印异常堆栈信息
            if (connection != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    connection.rollback();
                } catch (SQLException excep) {
                    excep.printStackTrace();
                }
            }
            return 0;
        } finally {
            // 恢复自动提交模式并关闭资源
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
} 