package com.zyf.servlets;

import com.google.gson.Gson;
import com.zyf.dao.ParkingRecordDao;
import com.zyf.model.ParkingRecord;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/admin/report")
public class ReportServlet extends HttpServlet {
    private ParkingRecordDao parkingRecordDao = new ParkingRecordDao();
    private Gson gson = new Gson();
    private Map<String, Map<String, Double>> cache = new HashMap<>();
    private long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            String type = request.getParameter("type");
            if (type == null || type.trim().isEmpty()) {
                type = "year";
            }

            // 检查缓存是否过期
            if (System.currentTimeMillis() - lastCacheTime > CACHE_DURATION) {
                updateCache();
            }

            // 从缓存返回数据
            Map<String, Double> result = cache.getOrDefault(type, new TreeMap<>());
            response.getWriter().write(gson.toJson(result));
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private synchronized void updateCache() {
        try {
            // 获取最近一年的数据
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, -1);
            List<ParkingRecord> records = parkingRecordDao.getPaidRecordsSince(cal.getTime());

            // 初始化不同时间维度的数据
            Map<String, Double> yearData = initializeYearlyData();
            Map<String, Double> monthData = initializeMonthlyData();
            Map<String, Double> dayData = initializeDailyData();

            // 格式化工具
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

            // 一次遍历，更新所有时间维度的数据
            for (ParkingRecord record : records) {
                Date entryTime = record.getEntryTime();
                double cost = record.getCost();

                yearData.merge(yearFormat.format(entryTime), cost, Double::sum);
                monthData.merge(monthFormat.format(entryTime), cost, Double::sum);
                dayData.merge(dayFormat.format(entryTime), cost, Double::sum);
            }

            // 更新缓存
            cache.put("year", yearData);
            cache.put("month", monthData);
            cache.put("day", dayData);
            lastCacheTime = System.currentTimeMillis();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Double> initializeYearlyData() {
        Map<String, Double> data = new TreeMap<>();
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        for (int i = 4; i >= 0; i--) {
            data.put(String.valueOf(currentYear - i), 0.0);
        }
        return data;
    }

    private Map<String, Double> initializeMonthlyData() {
        Map<String, Double> data = new TreeMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        for (int i = 11; i >= 0; i--) {
            data.put(formatter.format(cal.getTime()), 0.0);
            cal.add(Calendar.MONTH, -1);
        }
        return data;
    }

    private Map<String, Double> initializeDailyData() {
        Map<String, Double> data = new TreeMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 29; i >= 0; i--) {
            data.put(formatter.format(cal.getTime()), 0.0);
            cal.add(Calendar.DATE, -1);
        }
        return data;
    }
}