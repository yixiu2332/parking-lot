package com.zyf.partinglot.message;

// MyWebSocketService.java
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.zyf.partinglot.utils.EmptyJSON;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;

import java.lang.reflect.Executable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyWebSocketService extends Service {

    private final String TAG = "MyWebSocketService";
    private List<MyCallback> callbacks = new ArrayList<>(); // 存储多个回调

    ExecutorService executor = Executors.newSingleThreadExecutor();
    // 定义回调接口
    public interface MyCallback {
        void onDataReceived(String message);
    }
    // 注册回调
    public void registerCallback(MyCallback callback) {
        if (callback != null && !callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    // 取消注册回调
    public void unregisterCallback(MyCallback callback) {
        callbacks.remove(callback);
    }
    // 向所有注册的 Activity 发送数据
    public void sendDataToAllActivities(String data) {
        for (MyCallback callback : callbacks) {
            callback.onDataReceived(data);
        }
    }
    //接受activity数据并向服务器发送，供activity调用
    public void sendStringToServer(String message){
        executor.submit(() -> webSocketClient.send(message));
    }

    private WebSocketClient webSocketClient = null;
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MyWebSocketService getService() {
            return MyWebSocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(webSocketClient==null){
            connectWebSocket();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void connectWebSocket() {
        URI uri;
        try {
//                  uri = new URI("ws://10.0.2.2:8080/websocket");
             uri = new URI("ws://47.122.74.5:8080/websocket");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                showToast("WebSocket Opened");
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "onMessage: "+message);
                //把接受到的json发送给各个activity处理
               sendDataToAllActivities(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                
            }

            @Override
            public void onError(Exception ex) {
                Log.d(TAG, "onError: "+ex.getMessage());
            }
        };
        webSocketClient.connect();
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        executor.shutdown();

    }

}