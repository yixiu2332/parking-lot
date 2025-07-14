package com.zyf.message;

import org.json.JSONObject;


import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket")
public class WebSocketServer {
    private static final WebSocketResourceManager resourceManager = WebSocketResourceManager.getInstance();
    private static final WebSocketMessageHandler messageHandler = WebSocketMessageHandler.getInstance();
    // 存储用户ID和对应的WebSocket会话
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    // 添加SocketServer引用
    private static final SocketServer socketServer = SocketServer.getInstance();

    // 在类初始化时设置SocketMessageHandler的WebSocketServer实例
    static {
        SocketMessageHandler.getInstance().setWebSocketServer(getInstance());
    }

    // 单例实例
    private static WebSocketServer instance;

    // 获取单例实例
    public static synchronized WebSocketServer getInstance() {
        if (instance == null) {
            instance = new WebSocketServer();
        }
        return instance;
    }

    @OnOpen
    public void onOpen(Session session) {
        if (session != null) {
            resourceManager.getSessions().add(session);
            System.out.println("New connection: " + session.getId());

        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println("Message from " + session.getId() + ": " + message);
        resourceManager.getExecutorService().submit(() -> {
            try {
                // 示例：如何使用SocketServer发送消息给Socket设备
                // socketServer.sendMessage("deviceId", "your message");
                
                JSONObject root = messageHandler.processMessages(message);
                if (root != null) {
                    session.getBasicRemote().sendText(root.toString());
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        });
    }

    @OnClose
    public void onClose(Session session) {
        resourceManager.getSessions().remove(session);
        
        System.out.println("Connection closed: " + session.getId());
    }

    @OnError
    public void onError(Throwable error, Session session) {
        System.err.println("Error on session " + session.getId() + ": " + error.getMessage());
    }
    
    /**
     * 向所有WebSocket客户端广播消息
     */
    public void broadcastMessage(String message) {
        for (Session session : resourceManager.getSessions()) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                System.err.println("广播消息时出错: " + e.getMessage());
            }
        }
    }
    
    /**
     * 向指定用户发送消息
     */
    public void sendMessage(String userId, String message) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                System.out.println("向用户 " + userId + " 发送消息: " + message);
            } catch (IOException e) {
                System.err.println("向用户 " + userId + " 发送消息时出错: " + e.getMessage());
            }
        } else {
            System.out.println("用户 " + userId + " 未连接，无法发送消息");
        }
    }

    /**
     * 获取SocketServer实例，用于发送消息给Socket设备
     */
    public SocketServer getSocketServer() {
        return socketServer;
    }
}