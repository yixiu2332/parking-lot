package com.zyf.partinglot.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zyf.partinglot.R;
import com.zyf.partinglot.adapter.HistoryAdapter;
import com.zyf.partinglot.message.MyWebSocketService;
import com.zyf.partinglot.utils.EmptyJSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements MyWebSocketService.MyCallback {

    private final String TAG = "HistoryActivity";
    private MyWebSocketService myService;
    private boolean isBound = false;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyWebSocketService.LocalBinder binder = (MyWebSocketService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            myService.registerCallback(HistoryActivity.this); // 注册回调

            JSONObject root;
            try {
                root = EmptyJSON.getForCommand();
                JSONObject data = new JSONObject();
                data.put("describe","history");
                data.put("userId",getIntent().getIntExtra("userId",0));
                root.put("data",data);
                myService.sendStringToServer(root.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
    private ArrayList<String[]> list = new ArrayList<>();
    private HistoryAdapter adapter;


    @Override
    public void onDataReceived(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject root = new JSONObject(message);
                    JSONObject data = root.getJSONObject("data");
                    if(data.getString("describe").equals("history")){
                        Log.d(TAG, message);
                        for(int i =1;i<=data.getInt("count");i++){
                            JSONObject record = data.getJSONObject("record" + i);
                            String[] strings = new String[4];
                            strings[0] = record.getString("date");
                            strings[1] = record.getLong("time")+"分钟";
                            strings[2] = "parking01";
                            strings[3] = "space"+record.getInt("space");
                            list.add(strings);
                        }
                        adapter.updateData(list);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 绑定服务
        Intent intent = new Intent(this, MyWebSocketService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        //back
        findViewById(R.id.ibt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // 初始化 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 设置布局管理器

        adapter = new HistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter); // 将适配器绑定到 RecyclerView}
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            myService.unregisterCallback(this); // 取消注册回调
            unbindService(connection);
            isBound = false;
        }
    }
}