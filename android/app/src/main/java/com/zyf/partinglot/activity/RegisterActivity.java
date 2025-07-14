package com.zyf.partinglot.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.zyf.partinglot.R;
import com.zyf.partinglot.message.MyWebSocketService;
import com.zyf.partinglot.utils.EmptyJSON;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements MyWebSocketService.MyCallback {
    private final String TAG = "RegisterActivity";
    private MyWebSocketService myService;
    private boolean isBound = false;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyWebSocketService.LocalBinder binder = (MyWebSocketService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            myService.registerCallback(RegisterActivity.this); // 注册回调
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    public void onDataReceived(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject root = new JSONObject(message);
                    JSONObject data = root.getJSONObject("data");
                    if(data.getString("describe").equals("UserRegister")){
                        switch (data.getString("reply")){
                            case "yes":
                                showToast("注册成功");
                                finish();
                                break;
                            case "noAccount":
                                showToast("账号已被注册");
                                break;
                            case "noPhone":
                                showToast("手机号已被注册");
                                break;
                        }
                        bt_register.setEnabled(true);
                        progress.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }
    private ProgressBar progress;
    private Button bt_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 绑定服务
        Intent intent = new Intent(this, MyWebSocketService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //获取控件对象
        progress = findViewById(R.id.progress);
        ImageButton ibt_back = findViewById(R.id.ibt_back);
        EditText et_name = findViewById(R.id.et_name);
        EditText et_account = findViewById(R.id.et_account);
        EditText et_password = findViewById(R.id.et_password);
        EditText et_phone = findViewById(R.id.et_phone);
        bt_register = findViewById(R.id.bt_register);
        //设置监听事件
        ibt_back.setOnClickListener(v -> finish());
        bt_register.setOnClickListener(v -> {
            String name = et_name.getText().toString();
            String account = et_account.getText().toString();
            String password = et_password.getText().toString();
            String phone = et_phone.getText().toString();

            if(name.isEmpty()||account.isEmpty()||password.isEmpty()){
                Toast.makeText(RegisterActivity.this, "请输入完整信息", Toast.LENGTH_SHORT).show();
                return;
            }
            if(phone.length()!=11){
                Toast.makeText(RegisterActivity.this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            bt_register.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            try {
                JSONObject root = EmptyJSON.getForCommand();
                JSONObject data = new JSONObject();
                data.put("describe","UserRegister");
                data.put("name",name);
                data.put("account",account);
                data.put("password",password);
                data.put("phone",phone);
                root.put("data",data);
                myService.sendStringToServer(root.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });
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

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

}