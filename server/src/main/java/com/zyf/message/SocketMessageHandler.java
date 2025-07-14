package com.zyf.message;

import java.util.logging.Logger;
import org.json.JSONObject;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 * Socket消息处理器
 * 用于处理来自外设的消息，并与WebSocket服务器进行交互
 */
public class SocketMessageHandler {
    private static final Logger logger = Logger.getLogger(SocketMessageHandler.class.getName());
    private static SocketMessageHandler instance;
    private WebSocketServer webSocketServer;
    
    // 图像传输相关常量
    private static final String IMAGE_START_FLAG = "IMG_START";
    private static final int IMAGE_WIDTH = 320;
    private static final int IMAGE_HEIGHT = 240;
    private static final int BYTES_PER_PIXEL = 2; // RGB565格式每像素2字节
    
    // 存储设备ID和对应的图像保存路径
    private String imageSavePath = "images";

    private SocketMessageHandler() {
        // 私有构造函数，使用单例模式
        logger.info("SocketMessageHandler 初始化");
        // 创建图像保存目录
        File imageDir = new File(imageSavePath);
        if (!imageDir.exists()) {
            imageDir.mkdirs();
            logger.info("创建图像保存目录: " + imageSavePath);
        }
    }

    public static synchronized SocketMessageHandler getInstance() {
        if (instance == null) {
            instance = new SocketMessageHandler();
        }
        return instance;
    }

    /**
     * 设置WebSocket服务器实例
     */
    public void setWebSocketServer(WebSocketServer webSocketServer) {
        this.webSocketServer = webSocketServer;
    }

    /**
     * 处理来自外设的消息
     */
    public String handleMessage(String deviceId, String message) {
        // 如果是图像数据，直接处理
        if (message != null && !message.startsWith("{")) {
            String result = handleImageData(deviceId, message);
            return result;
        } else {
            logger.info("收到非图像数据: " + message);
        }
        
        try {
            // 尝试解析JSON消息
            JSONObject jsonMessage = new JSONObject(message);
            String messageType = jsonMessage.optString("type", "");
            
            switch (messageType) {
                // ... 其他消息处理 ...
            }
        } catch (Exception e) {
            // 如果不是JSON格式，可能是其他格式的消息
            logger.info("非JSON格式消息: " + message);
            return null;
        }
        return deviceId;
    }
    
    /**
     * 处理图像数据
     */
    private String handleImageData(String deviceId, String hexData) {
        try {
            hexData = hexData.trim();
            
            if (hexData.isEmpty()) {
                logger.warning("收到空的图像数据");
                return null;
            }
            byte[] data = hexStringToByteArray(hexData);
            
            // 创建BufferedImage
            BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
            
            // 处理图像数据
            for (int y = 0; y < IMAGE_HEIGHT; y++) {
                for (int x = 0; x < IMAGE_WIDTH; x++) {
                    int pixelIndex = y * IMAGE_WIDTH + x;
                    int dataIndex = pixelIndex * 2;
                    
                    if (dataIndex + 1 < data.length) {
                        // 读取RGB565像素
                        int pixel = (data[dataIndex] & 0xFF) | ((data[dataIndex + 1] & 0xFF) << 8);
                        
                        // 转换为RGB888
                        int r = ((pixel >> 11) & 0x1F) << 3;
                        int g = ((pixel >> 5) & 0x3F) << 2;
                        int b = (pixel & 0x1F) << 3;
                        
                        // 设置像素
                        int rgb = (r << 16) | (g << 8) | b;
                        image.setRGB(x, y, rgb);
                    } else {
                        // 数据不足时使用白色填充
                        image.setRGB(x, y, 0xFFFFFF);
                    }
                }
            }
            
            // 生成文件名并保存图像
            String fileName = "image_" + deviceId + "_" + System.currentTimeMillis() + ".jpg";
            File outputFile = new File(imageSavePath, fileName);
            ImageIO.write(image, "jpg", outputFile);
            logger.info("图像处理完成，保存为: " + fileName);

            // 将图像转换为base64编码
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            // 通知WebSocket客户端
            if (webSocketServer != null) {
                JSONObject notification = new JSONObject();
                JSONObject jsonData = new JSONObject();
                jsonData.put("describe", "ov7670");
                jsonData.put("image", base64Image);
                notification.put("data", jsonData);
                webSocketServer.broadcastMessage(notification.toString());
                logger.info("websocket send end");
            }
            
            // 清理资源
            baos.close();
            
            return null;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "处理图像数据时出错", e);
            e.printStackTrace();  // 添加这行以打印完整的堆栈跟踪
            return null;
        }
    }
    
    /**
     * 将16进制字符串转换为字节数组
     */
    private byte[] hexStringToByteArray(String hexString) {
        // 移除所有非16进制字符
        hexString = hexString.replaceAll("[^0-9A-Fa-f]", "");
        
        // 确保长度是偶数
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }
        
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                        + Character.digit(hexString.charAt(i + 1), 16));
            }
        } catch (Exception e) {
            logger.warning("解析16进制字符串时出错: " + e.getMessage() + ", 字符串: " + hexString);
            throw e;
        }
        
        return data;
    }
    

} 
 