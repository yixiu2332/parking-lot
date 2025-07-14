package com.zyf.partinglot.message;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MyForegroundService extends Service {
    private static final String TAG = "MyForegroundService";
    private static final int SOCKET_PORT = 8086;  // 监听端口
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    public static boolean isRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        executorService = Executors.newFixedThreadPool(3); // 创建一个线程池来处理客户端连接
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        // 启动Socket服务器
        startSocketServer();
        return START_STICKY;  // 保持服务运行，即使系统杀死它
    }

    // 启动Socket服务器
    private void startSocketServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(SOCKET_PORT, 0, InetAddress.getByName("0.0.0.0"));
                Log.d(TAG, "Server started, listening on port " + SOCKET_PORT);

                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();  // 接受客户端连接
                        Log.d(TAG, "Accepted connection from " + clientSocket.getInetAddress());
                        Log.d(TAG, "serverSocket IP :"+Inet4Address.getLocalHost());
                        // 使用线程池来处理客户端连接
                        executorService.submit(() -> handleSocketClient(clientSocket));

                    } catch (IOException e) {
                        if (!serverSocket.isClosed()) {
                            Log.e(TAG, "Error accepting socket: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error in Socket Server: " + e.getMessage());
            }
        }).start();
    }

    // 处理客户端请求
    private void handleSocketClient(Socket clientSocket) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String message;
            while ((message = input.readLine()) != null) {
                Log.d(TAG, "Received: " + message);
                output.println("Echo: " + message);  // 向客户端发送回显信息
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing client socket: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executorService != null) {
                executorService.shutdown();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing server: " + e.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // 返回null，因为我们不需要绑定这个服务
        return null;
    }
}
