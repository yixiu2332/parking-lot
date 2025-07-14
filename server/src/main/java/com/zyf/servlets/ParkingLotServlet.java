package com.zyf.servlets;

import com.google.gson.Gson;
import com.zyf.dao.ParkingLotDao;
import com.zyf.dao.ParkingSpaceDao;
import com.zyf.model.ParkingLot;
import com.zyf.model.ParkingSpace;
import com.zyf.message.WebSocketResourceManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet("/admin/parking-lots/*")
public class ParkingLotServlet extends HttpServlet {
    private ParkingLotDao parkingLotDao = new ParkingLotDao();
    private ParkingSpaceDao parkingSpaceDao = new ParkingSpaceDao();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取所有停车场列表
            List<ParkingLot> parkingLots = parkingLotDao.getAllParkingLots();
            response.getWriter().write(gson.toJson(parkingLots));
        } else {
            // 获取单个停车场信息
            try {
                int parkingLotId = Integer.parseInt(pathInfo.substring(1));
                ParkingLot parkingLot = parkingLotDao.getParkingLotById(parkingLotId);
                if (parkingLot != null) {
                    response.getWriter().write(gson.toJson(parkingLot));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\":\"停车场不存在\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"无效的停车场ID\"}");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            String requestBody = request.getReader().lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual);
            
            if (requestBody == null || requestBody.trim().isEmpty()) {
                response.getWriter().write("{\"success\":false,\"message\":\"请求数据不能为空\"}");
                return;
            }

            ParkingLot parkingLot = gson.fromJson(requestBody, ParkingLot.class);
            
            // 参数验证
            if (parkingLot == null) {
                response.getWriter().write("{\"success\":false,\"message\":\"无效的请求数据格式\"}");
                return;
            }
            
            if (parkingLot.getName() == null || parkingLot.getName().trim().isEmpty()) {
                response.getWriter().write("{\"success\":false,\"message\":\"停车场名称不能为空\"}");
                return;
            }
            if (parkingLot.getAddress() == null || parkingLot.getAddress().trim().isEmpty()) {
                response.getWriter().write("{\"success\":false,\"message\":\"停车场地址不能为空\"}");
                return;
            }
            if (parkingLot.getTotalSpaces() <= 0) {
                response.getWriter().write("{\"success\":false,\"message\":\"总车位数必须大于0\"}");
                return;
            }
            if (parkingLot.getRate1() < 0 || parkingLot.getRate2() < 0) {
                response.getWriter().write("{\"success\":false,\"message\":\"费率不能为负数\"}");
                return;
            }

            // 设置初始可用车位数等于总车位数
            parkingLot.setAvailableSpaces(parkingLot.getTotalSpaces());

            boolean success = parkingLotDao.insertParkingLot(parkingLot);
            
            if (success) {
                // 获取当前停车场已有的车位
                List<ParkingSpace> existingSpaces = parkingSpaceDao.getParkingSpacesByLotId(parkingLot.getParkingLotId());
                int startNumber = existingSpaces.size() + 1;
                
                // 自动创建对应数量的停车位
                for (int i = startNumber; i <= startNumber + parkingLot.getTotalSpaces() - 1; i++) {
                    ParkingSpace space = new ParkingSpace();
                    space.setParkingLotId(parkingLot.getParkingLotId());
                    space.setSpaceNumber(String.format("%03d", i));
                    space.setStatus(0); // 默认可用
                    space.setLockStatus(0); // 默认未锁定
                    space.setReservedBy(null); // 默认无人预约
                    parkingSpaceDao.insertParkingSpace(space);
                }
                
                // 在新增停车场后也触发广播
                WebSocketResourceManager.setTimerTask(true);
                
                response.getWriter().write("{\"success\":true,\"message\":\"停车场添加成功\"}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"停车场添加失败\"}");
            }
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\":false,\"message\":\"数据格式错误：请检查输入的数字格式\"}");
        } catch (Exception e) {
            response.getWriter().write("{\"success\":false,\"message\":\"系统错误：" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            ParkingLot parkingLot = gson.fromJson(request.getReader(), ParkingLot.class);
            
            // 参数验证
            if (parkingLot.getParkingLotId() <= 0) {
                response.getWriter().write("{\"success\":false,\"message\":\"无效的停车场ID\"}");
                return;
            }
            if (parkingLot.getName() == null || parkingLot.getName().trim().isEmpty()) {
                response.getWriter().write("{\"success\":false,\"message\":\"停车场名称不能为空\"}");
                return;
            }
            if (parkingLot.getAddress() == null || parkingLot.getAddress().trim().isEmpty()) {
                response.getWriter().write("{\"success\":false,\"message\":\"停车场地址不能为空\"}");
                return;
            }
            if (parkingLot.getTotalSpaces() <= 0) {
                response.getWriter().write("{\"success\":false,\"message\":\"总车位数必须大于0\"}");
                return;
            }
            if (parkingLot.getRate1() < 0 || parkingLot.getRate2() < 0) {
                response.getWriter().write("{\"success\":false,\"message\":\"费率不能为负数\"}");
                return;
            }

            // 检查是否存在
            ParkingLot existingLot = parkingLotDao.getParkingLotById(parkingLot.getParkingLotId());
            if (existingLot == null) {
                response.getWriter().write("{\"success\":false,\"message\":\"停车场不存在\"}");
                return;
            }

            // 检查车位数量变化
            if (parkingLot.getTotalSpaces() < existingLot.getTotalSpaces()) {
                response.getWriter().write("{\"success\":false,\"message\":\"不能减少停车位数量\"}");
                return;
            }
            
            boolean success = parkingLotDao.updateParkingLot(parkingLot);
            
            if (success) {
                // 如果增加了车位数，创建新的车位
                if (parkingLot.getTotalSpaces() > existingLot.getTotalSpaces()) {
                    // 获取当前停车场已有的车位
                    List<ParkingSpace> existingSpaces = parkingSpaceDao.getParkingSpacesByLotId(parkingLot.getParkingLotId());
                    int startNumber = existingSpaces.size() + 1;
                    
                    for (int i = startNumber; i <= startNumber + (parkingLot.getTotalSpaces() - existingLot.getTotalSpaces()) - 1; i++) {
                        ParkingSpace space = new ParkingSpace();
                        space.setParkingLotId(parkingLot.getParkingLotId());
                        space.setSpaceNumber(String.format("%03d", i));
                        space.setStatus(0); // 默认可用
                        space.setLockStatus(0); // 默认未锁定
                        space.setReservedBy(null); // 默认无人预约
                        parkingSpaceDao.insertParkingSpace(space);
                    }
                }
                
                // 设置定时任务标志位为true，触发广播
                WebSocketResourceManager.setTimerTask(true);
                
                response.getWriter().write("{\"success\":true,\"message\":\"停车场信息更新成功\"}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"停车场信息更新失败\"}");
            }
        } catch (Exception e) {
            response.getWriter().write("{\"success\":false,\"message\":\"系统错误：" + e.getMessage() + "\"}");
        }
    }
} 