package com.zyf.message;

import com.zyf.dao.ParkingLotDao;
import com.zyf.model.ParkingLot;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Socket服务器类，用于监听外设的Socket连接
 */
public class SocketServer {
    private static final Logger logger = Logger.getLogger(SocketServer.class.getName());
    private static final int PORT = 8086;
    private static SocketServer instance;
    private ServerSocket serverSocket;
    private boolean running = false;
    private ExecutorService executorService;
    // 存储设备ID和对应的Socket连接
    private ConcurrentHashMap<String, Socket> deviceConnections = new ConcurrentHashMap<>();
    // 存储设备ID和对应的消息处理器
    private ConcurrentHashMap<String, PrintWriter> deviceWriters = new ConcurrentHashMap<>();
    // 图像数据缓冲区大小
    private static final int BUFFER_SIZE = 8192;

    private SocketServer() {
        // 私有构造函数，使用单例模式
    }

    public static synchronized SocketServer getInstance() {
        if (instance == null) {
            instance = new SocketServer();
        }
        return instance;
    }

    /**
     * 启动Socket服务器
     */
    public void start() {
        if (running) {
            logger.info("Socket服务器已经在运行中");
            return;
        }
        logger.info("Socket服务器正在监听8086端口");
        executorService = Executors.newCachedThreadPool();
        running = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        // 设置Socket参数，优化大数据传输
                        clientSocket.setReceiveBufferSize(BUFFER_SIZE);
                        clientSocket.setSendBufferSize(BUFFER_SIZE);
                        clientSocket.setTcpNoDelay(true);
                        clientSocket.setKeepAlive(true);
                        
                        executorService.execute(new ClientHandler(clientSocket));
                        logger.info("新的外设连接: " + clientSocket.getInetAddress().getHostAddress());
                    } catch (IOException e) {
                        if (running) {
                            logger.log(Level.SEVERE, "接受客户端连接时出错", e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "启动Socket服务器时出错", e);
            }
        }).start();
    }

    /**
     * 停止Socket服务器
     */
    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "关闭服务器Socket时出错", e);
            }
        }

        // 关闭所有客户端连接
        for (Socket socket : deviceConnections.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "关闭客户端Socket时出错", e);
            }
        }

        deviceConnections.clear();
        deviceWriters.clear();

        if (executorService != null) {
            executorService.shutdown();
        }

        logger.info("Socket服务器已停止");
    }

    /**
     * 向指定设备发送消息
     */
    public void sendMessage(String deviceId, String message) {
        PrintWriter writer = deviceWriters.get(deviceId);
        if (writer != null) {
            writer.println("JSONStart");
            writer.println(message);
            writer.println("JSONStop");
            writer.flush();
            logger.info("向设备 " + deviceId + " 发送消息: " + message);
        } else {
            logger.warning("设备 " + deviceId + " 未连接，无法发送消息");
        }
    }

    /**
     * 获取所有已连接的设备
     */
    public ConcurrentHashMap<String, Socket> getDeviceConnections() {
        return deviceConnections;
    }

    /**
     * 客户端处理器，每个客户端连接都会创建一个处理器
     */
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader reader;
        private PrintWriter writer;
        private String deviceId;
        private boolean isReceivingImageData = false;
        private StringBuilder imageDataBuffer = new StringBuilder();
        private long lastImageDataTime = 0;
        private long imageStartTime = 0;
        private static final long IMAGE_TIMEOUT = 5000; // 5秒超时

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()), BUFFER_SIZE);
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                // 读取设备ID
                String firstMessage = reader.readLine();
                if (firstMessage != null && firstMessage.startsWith("DEVICE_ID:")) {
                    deviceId = firstMessage.substring(10);
                    deviceConnections.put(deviceId, clientSocket);
                    deviceWriters.put(deviceId, writer);
                    logger.info("设备 " + deviceId + " 已连接");
                    JSONObject root = new JSONObject();
                    root.put("type", "info");
                    ParkingLotDao parkingLotDao = new ParkingLotDao();
                    ParkingLot parkingLotById = parkingLotDao.getParkingLotById(1);
                    root.put("cost1", parkingLotById.getRate1());
                    root.put("cost2", parkingLotById.getRate2());
                    root.put("empty", parkingLotById.getAvailableSpaces());
                    root.put("max", parkingLotById.getTotalSpaces());
                    sendMessage(deviceId, root.toString());
                } else {
                    logger.warning("无效的设备ID格式，关闭连接");
                    clientSocket.close();
                    return;
                }

                // 持续读取客户端消息
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    // 检查是否是图像开始标志
                    if (inputLine.startsWith("IMG_START")) {
                        imageStartTime = System.currentTimeMillis();
                        isReceivingImageData = true;
                        logger.info("接收到："+inputLine+",读取图片中\n");
                        // 读取下一行作为图像数据
                        String imageData = reader.readLine();
                        logger.info("读取完成");
                        if (imageData != null) {
                            logger.info("准备处理图像数据，长度: " + imageData.length());
                            SocketMessageHandler.getInstance().handleMessage(deviceId, imageData);
                            logger.info("图像处理完成");
                        }
                        
                        isReceivingImageData = false;
                    } else {
                        // 处理普通消息
                        logger.info("收到来自设备 " + deviceId + " 的消息: " + inputLine);
                        processMessage(deviceId, inputLine);
                    }
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "客户端连接异常", e);
            } finally {
                try {
                    if (deviceId != null) {
                        deviceConnections.remove(deviceId);
                        deviceWriters.remove(deviceId);
                        logger.info("设备 " + deviceId + " 已断开连接");
                    }
                    clientSocket.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "关闭客户端Socket时出错", e);
                }
            }
        }

        /**
         * 处理图像数据块
         */
        private void handleImageDataChunk(String dataChunk) {
            // 更新最后接收时间
            lastImageDataTime = System.currentTimeMillis();
            
            // 检查是否是图像结束标志
            if (dataChunk.equals("IMG_END")) {
                isReceivingImageData = false;
                logger.info("图像数据接收完成，总长度: " + imageDataBuffer.length());
                
                // 处理完整的图像数据
                String response = processMessage(deviceId, imageDataBuffer.toString());
                
                // 发送确认消息
                if (response != null && !response.isEmpty()) {
                    writer.println(response);
                    writer.flush();
                }
                
                imageDataBuffer = null; // 释放内存
                return;
            }

            
            // 累积图像数据
            imageDataBuffer.append(dataChunk);
            
            // 如果数据量达到一定大小，发送确认消息
            if (imageDataBuffer.length() % 10000 == 0) {
                writer.println("DATA_ACK:" + imageDataBuffer.length());
                writer.flush();
                logger.info("已接收图像数据: " + imageDataBuffer.length() + " 字节");
            }
        }

        /**
         * 处理来自设备的消息
         */
        private String processMessage(String deviceId, String message) {
            try {
                // 使用SocketMessageHandler处理消息
                SocketMessageHandler messageHandler = SocketMessageHandler.getInstance();
                String response = messageHandler.handleMessage(deviceId, message);
                
                return response;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "处理消息时出错", e);
                // 返回错误响应
                return "{\"status\":\"error\",\"message\":\"服务器内部错误\"}";
            }
        }
    }
} 