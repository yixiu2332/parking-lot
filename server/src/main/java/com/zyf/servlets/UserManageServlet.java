package com.zyf.servlets;

import com.google.gson.Gson;
import com.zyf.dao.UserDao;
import com.zyf.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/users/*")
public class UserManageServlet extends HttpServlet {
    private UserDao userDao = new UserDao();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String searchTerm = request.getParameter("search");
        List<User> users = userDao.getAllUsers();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            users = users.stream()
                .filter(user -> 
                    user.getName().contains(searchTerm) || 
                    user.getPhone().contains(searchTerm))
                .collect(Collectors.toList());
        }
        
        response.getWriter().write(gson.toJson(users));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                response.getWriter().write("{\"success\":false,\"message\":\"缺少用户ID\"}");
                return;
            }
            
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 2) {
                response.getWriter().write("{\"success\":false,\"message\":\"无效的用户ID\"}");
                return;
            }
            
            int userId = Integer.parseInt(pathParts[1]);
            boolean success = userDao.deleteUser(userId);
            
            if (success) {
                response.getWriter().write("{\"success\":true,\"message\":\"用户删除成功\"}");
            } else {
                response.getWriter().write("{\"success\":false,\"message\":\"用户删除失败\"}");
            }
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"success\":false,\"message\":\"无效的用户ID格式\"}");
        } catch (Exception e) {
            response.getWriter().write("{\"success\":false,\"message\":\"系统错误：" + e.getMessage() + "\"}");
        }
    }
} 