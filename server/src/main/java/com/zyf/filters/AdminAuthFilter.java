package com.zyf.filters;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/admin/*", "/index.html"})
public class AdminAuthFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 获取请求的URI
        String uri = httpRequest.getRequestURI();
        
        // 如果是登录相关的请求，直接放行
        if (uri.contains("/admin/login") || uri.contains("/login.html")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 检查session中是否有管理员信息
        HttpSession session = httpRequest.getSession(false);
        if (session != null && session.getAttribute("adminId") != null) {
            // 已登录，继续请求
            chain.doFilter(request, response);
        } else {
            // 未登录，重定向到登录页面
            httpResponse.sendRedirect("/login.html");
        }
    }
    
    @Override
    public void destroy() {
    }
} 