package com.zyf.servlets;

import com.google.gson.Gson;
import com.zyf.dao.LicenseReviewDao;
import com.zyf.dao.UserDao;
import com.zyf.model.LicenseReview;
import com.zyf.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/license-reviews")
public class LicenseReviewServlet extends HttpServlet {
    private LicenseReviewDao licenseReviewDao = new LicenseReviewDao();
    private UserDao userDao = new UserDao();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        List<LicenseReview> reviews = licenseReviewDao.getPendingReviews();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (LicenseReview review : reviews) {
            Map<String, Object> reviewMap = new HashMap<>();
            User user = userDao.getUserById(review.getUserId());
            
            reviewMap.put("reviewId", review.getReviewId());
            reviewMap.put("username", user.getName());
            reviewMap.put("licensePlate", review.getLicensePlate());
            reviewMap.put("status", review.getStatus());
            reviewMap.put("submittedAt", review.getSubmittedAt());
            
            result.add(reviewMap);
        }
        
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取参数
            int reviewId = Integer.parseInt(request.getParameter("reviewId"));
            int status = Integer.parseInt(request.getParameter("status"));
            
            // 获取审核记录
            LicenseReview review = licenseReviewDao.getReviewById(reviewId);
            if (review == null) {
                result.put("success", false);
                result.put("message", "审核记录不存在");
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            // 检查当前状态是否为待审核
            if (review.getStatus() != 0) {
                result.put("success", false);
                result.put("message", "该记录已被审核");
                response.getWriter().write(gson.toJson(result));
                return;
            }
            
            // 更新审核状态
            if (licenseReviewDao.updateReviewStatus(reviewId, status)) {
                if (status == 1) { // 如果审核通过，更新用户的车牌号
                    User user = userDao.getUserById(review.getUserId());
                    if (user != null) {
                        user.setLicensePlate(review.getLicensePlate());
                        user.setUpdatedAt(new Date());
                        if (userDao.updateUser(user)) {
                            result.put("success", true);
                            result.put("message", "审核通过，已更新用户车牌号");
                        } else {
                            result.put("success", false);
                            result.put("message", "审核通过，但用户信息更新失败");
                        }
                    } else {
                        result.put("success", false);
                        result.put("message", "审核通过，但未找到用户信息");
                    }
                } else {
                    result.put("success", true);
                    result.put("message", "审核已拒绝");
                }
            } else {
                result.put("success", false);
                result.put("message", "审核状态更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
        }
        
        response.getWriter().write(gson.toJson(result));
    }
} 