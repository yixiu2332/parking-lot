package com.zyf.servlets;

import com.zyf.dao.AdminDao;
import com.zyf.model.Admin;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {
    private AdminDao adminDao = new AdminDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        // 设置响应类型
        response.setContentType("text/plain;charset=UTF-8");
        
        try {
            // 根据用户名查询管理员
            Admin admin = adminDao.getAdminByUsername(username);
            
            if (admin != null && admin.getPassword().equals(password)) {
                // 登录成功，创建session
                HttpSession session = request.getSession();
                session.setAttribute("adminId", admin.getAdminId());
                session.setAttribute("adminUsername", admin.getUsername());
                
                // 返回成功消息
                response.getWriter().write("success");
            } else {
                // 登录失败
                response.getWriter().write("fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // 如果有人直接访问登录servlet，重定向到登录页面
        response.sendRedirect("/login.html");
    }
} 