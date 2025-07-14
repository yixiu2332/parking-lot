package com.zyf.message;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

/**
 * Socket服务器初始化监听器
 * 在Web应用启动时初始化Socket服务器，在Web应用关闭时停止Socket服务器
 */
@WebListener
public class SocketServerInitializer implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(SocketServerInitializer.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Web应用启动，初始化Socket服务器...");
        try {
            // 获取Socket服务器实例并启动
            SocketServer socketServer = SocketServer.getInstance();
            socketServer.start();
            // 将Socket服务器实例存储在ServletContext中，以便其他组件可以访问
            sce.getServletContext().setAttribute("socketServer", socketServer);
            logger.info("Socket服务器初始化完成");
        } catch (Exception e) {
            logger.severe("初始化Socket服务器时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Web应用关闭，停止Socket服务器...");
        try {
            // 从ServletContext中获取Socket服务器实例并停止
            SocketServer socketServer = (SocketServer) sce.getServletContext().getAttribute("socketServer");
            if (socketServer != null) {
                socketServer.stop();
                logger.info("Socket服务器已停止");
            }
        } catch (Exception e) {
            logger.severe("停止Socket服务器时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 