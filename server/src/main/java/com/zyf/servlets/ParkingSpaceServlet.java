package com.zyf.servlets;

import com.google.gson.Gson;
import com.zyf.dao.ParkingSpaceDao;
import com.zyf.dao.ParkingLotDao;
import com.zyf.model.ParkingSpace;
import com.zyf.model.ParkingLot;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet("/admin/parking-spaces/*")
public class ParkingSpaceServlet extends HttpServlet {
    private ParkingSpaceDao parkingSpaceDao = new ParkingSpaceDao();
    private ParkingLotDao parkingLotDao = new ParkingLotDao();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取所有车位列表
            List<ParkingSpace> spaces = parkingSpaceDao.getAllParkingSpaces();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (ParkingSpace space : spaces) {
                Map<String, Object> spaceMap = new HashMap<>();
                ParkingLot lot = parkingLotDao.getParkingLotById(space.getParkingLotId());
                
                spaceMap.put("spaceId", space.getSpaceId());
                spaceMap.put("spaceNumber", space.getSpaceNumber());
                spaceMap.put("parkingLotName", lot != null ? lot.getName() : "未知停车场");
                spaceMap.put("status", space.getStatus());
                spaceMap.put("reservedBy", space.getReservedBy());
                
                result.add(spaceMap);
            }
            
            response.getWriter().write(gson.toJson(result));
        } else {
            // 获取单个车位信息
            try {
                int spaceId = Integer.parseInt(pathInfo.substring(1));
                ParkingSpace space = parkingSpaceDao.getParkingSpaceById(spaceId);
                if (space != null) {
                    response.getWriter().write(gson.toJson(space));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\":\"车位不存在\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"无效的车位ID\"}");
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"缺少车位ID\"}");
            return;
        }
        
        try {
            int spaceId = Integer.parseInt(pathInfo.substring(1));
            ParkingSpace currentSpace = parkingSpaceDao.getParkingSpaceById(spaceId);
            if (currentSpace == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"车位不存在\"}");
                return;
            }
            
            // 切换状态
            boolean success = parkingSpaceDao.updateSpaceStatus(
                spaceId,
                currentSpace.getStatus() == 0 ? 2 : 0  // 0:可用, 1:使用中, 2:停用
            );
            
            if (success) {
                response.getWriter().write("{\"success\":true,\"message\":\"车位状态更新成功\"}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"车位状态更新失败\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"无效的车位ID\"}");
        }
    }
} 