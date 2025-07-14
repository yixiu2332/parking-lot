package com.zyf.servlets;

import com.google.gson.Gson;
import com.zyf.dao.ParkingRecordDao;
import com.zyf.dao.UserDao;
import com.zyf.dao.ParkingSpaceDao;
import com.zyf.model.ParkingRecord;
import com.zyf.model.User;
import com.zyf.model.ParkingSpace;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.text.SimpleDateFormat;

@WebServlet("/admin/parking-records")
public class ParkingHistoryServlet extends HttpServlet {
    private ParkingRecordDao parkingRecordDao = new ParkingRecordDao();
    private UserDao userDao = new UserDao();
    private ParkingSpaceDao parkingSpaceDao = new ParkingSpaceDao();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // 获取分页参数
            int page = Integer.parseInt(request.getParameter("page"));
            int size = Integer.parseInt(request.getParameter("size"));
            String searchTerm = request.getParameter("search");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            
            // 获取所有记录
            List<ParkingRecord> allRecords = parkingRecordDao.getAllParkingRecords();
            List<Map<String, Object>> resultRecords = new ArrayList<>();
            
            // 处理每条记录
            for (ParkingRecord record : allRecords) {
                User user = userDao.getUserById(record.getUserId());
                ParkingSpace space = parkingSpaceDao.getParkingSpaceById(record.getSpaceId());
                
                // 创建结果对象
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put("username", user.getName());
                recordMap.put("licensePlate", user.getLicensePlate());
                recordMap.put("spaceNumber", space.getSpaceNumber());
                recordMap.put("entryTime", record.getEntryTime());
                recordMap.put("exitTime", record.getExitTime());
                recordMap.put("cost", record.getCost());
                recordMap.put("paid", record.getPaid());
                
                resultRecords.add(recordMap);
            }
            
            // 过滤记录
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                resultRecords = filterBySearchTerm(resultRecords, searchTerm);
            }
            if (startDate != null && endDate != null) {
                resultRecords = filterByDateRange(resultRecords, startDate, endDate);
            }
            
            // 计算分页
            int total = resultRecords.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            // 准备返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("records", resultRecords.subList(start, end));
            result.put("totalPages", (total + size - 1) / size);
            result.put("currentPage", page);
            result.put("totalRecords", total);
            
            response.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    private List<Map<String, Object>> filterBySearchTerm(List<Map<String, Object>> records, String searchTerm) {
        return records.stream()
            .filter(record -> 
                String.valueOf(record.get("username")).contains(searchTerm) ||
                String.valueOf(record.get("licensePlate")).contains(searchTerm))
            .collect(java.util.stream.Collectors.toList());
    }
    
    private List<Map<String, Object>> filterByDateRange(List<Map<String, Object>> records, String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            
            return records.stream()
                .filter(record -> {
                    Date entryTime = (Date) record.get("entryTime");
                    return !entryTime.before(start) && !entryTime.after(end);
                })
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            return records;
        }
    }
} 