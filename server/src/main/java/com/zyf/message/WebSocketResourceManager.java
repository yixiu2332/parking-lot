package com.zyf.message;

import java.net.Socket;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;

import com.zyf.dao.ParkingLotDao;
import com.zyf.model.ParkingLot;
import org.json.JSONObject;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketResourceManager {
    private static volatile WebSocketResourceManager instance;
    private static volatile boolean timerTask = false;
    private final Set<Session> sessions = new CopyOnWriteArraySet<>();
    private final ExecutorService executorService;
    private final Timer timer;
    private final WebSocketMessageHandler messageHandler;
    private TimerTask task;

    private WebSocketResourceManager() {
        if (instance != null) {
            throw new IllegalStateException("实例已存在");
        }
        
        this.executorService = Executors.newFixedThreadPool(5);
        this.timer = new Timer(true);
        this.messageHandler = WebSocketMessageHandler.getInstance();
        
        initializeTimerTask();
    }

    private void initializeTimerTask() {
        task = new TimerTask() {
            @Override
            public void run() {
                if(timerTask) {
                    timerTask = false;
                    
                    try {
                        // 准备广播消息
                        JSONObject root = new JSONObject();
                        root.put("type", "info");
                        ParkingLotDao parkingLotDao = new ParkingLotDao();
                        ParkingLot parkingLotById = parkingLotDao.getParkingLotById(1);
                        root.put("cost1", parkingLotById.getRate1());
                        root.put("cost2", parkingLotById.getRate2());
                        root.put("empty", parkingLotById.getAvailableSpaces());
                        root.put("max", parkingLotById.getTotalSpaces());
                        
                        String message = root.toString();
                        
                        // 向WebSocket客户端广播
                        Set<Session> currentSessions = new CopyOnWriteArraySet<>(sessions);
                        for (Session session : currentSessions) {
                            try {
                                if (session.isOpen()) {
                                    JSONObject wsMessage = messageHandler.processHomeRequestData(null);
                                    session.getBasicRemote().sendText(wsMessage.toString());
                                } else {
                                    sessions.remove(session);
                                }
                            } catch (IOException e) {
                                System.err.println("定时任务发送WebSocket消息失败: " + e.getMessage());
                                sessions.remove(session);
                            }
                        }
                        
                        // 向Socket设备广播
                        SocketServer socketServer = SocketServer.getInstance();
                        // 获取所有已连接的设备ID
                        ConcurrentHashMap<String, Socket> deviceConnections = socketServer.getDeviceConnections();
                        for (String deviceId : deviceConnections.keySet()) {
                            socketServer.sendMessage(deviceId, message);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("定时任务处理失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        };
        timer.schedule(task, 1000, 100);
    }

    public static WebSocketResourceManager getInstance() {
        if (instance == null) {
            synchronized (WebSocketResourceManager.class) {
                if (instance == null) {
                    instance = new WebSocketResourceManager();
                }
            }
        }
        return instance;
    }

    public Set<Session> getSessions() {
        return sessions;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Timer getTimer() {
        return timer;
    }

    public static boolean isTimerTask() {
        return timerTask;
    }

    public static void setTimerTask(boolean timerTask) {
        WebSocketResourceManager.timerTask = timerTask;
    }

    public void shutdown() {
        task.cancel();
        executorService.shutdown();
        timer.cancel();
        sessions.clear();
    }
} 