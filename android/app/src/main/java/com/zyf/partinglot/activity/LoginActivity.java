package com.zyf.partinglot.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.zyf.partinglot.R;
import com.zyf.partinglot.message.MyWebSocketService;
import com.zyf.partinglot.utils.EmptyJSON;
import com.zyf.partinglot.utils.VerificationCodeGenerator;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements MyWebSocketService.MyCallback {
    private final String TAG = "LoginActivity";
    private String captcha;
    private MyWebSocketService myService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyWebSocketService.LocalBinder binder = (MyWebSocketService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            myService.registerCallback(LoginActivity.this); // 注册回调
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
    private ProgressBar login_progress;
    private Button bt_login;

    @Override
    public void onDataReceived(String message) {
        Log.d(TAG, "onDataReceived: "+message);
        runOnUiThread(() -> {
            try {
                JSONObject root = new JSONObject(message);
                JSONObject data = root.getJSONObject("data");
                switch (data.getString("describe")){
                    case "UserLoginVerification":
                        if(data.getString("reply").equals("yes")){
                            int userId = data.getInt("userId");
                            saveLoginStatus(userId);
                            EnterHomeActivity(userId);
                        }
                        else{
                            showToast("账号或密码错误！");
                        }
                        break;
                    case "UserLoginVerificationByCaptcha":
                        if (data.getString("reply").equals("yes")){
                            int userId = data.getInt("userId");
                            saveLoginStatus(userId);
                            EnterHomeActivity(userId);
                        } else if (data.getString("reply").equals("noPhone")) {
                            showToast("手机号还未注册！");
                        }else{
                            showToast("验证码错误！");
                        }
                        break;
                    default:

                }
                login_progress.setVisibility(View.GONE);
                bt_login.setEnabled(true);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean("isLoggedIn",false)){
            EnterHomeActivity(sharedPreferences.getInt("userId",0));
        }
        setContentView(R.layout.activity_login);
        //获取控件对象
        login_progress = findViewById(R.id.login_progress);
        ImageButton ibn_back = findViewById(R.id.ibt_back);
        EditText et_top = findViewById(R.id.et_top);
        EditText et_down = findViewById(R.id.et_down);
        RadioGroup rg_switch = findViewById(R.id.rg_switch);
        RadioButton rb_pwd = findViewById(R.id.rb_pwd);
        RadioButton rb_captcha = findViewById(R.id.rb_captcha);
        TextView tv_get = findViewById(R.id.tv_get);
        TextView tv_register = findViewById(R.id.tv_register);
        bt_login = findViewById(R.id.bt_login);
        TextView tv_manager = findViewById(R.id.tv_manager);
        //设置点击监听
        ibn_back.setOnClickListener(v -> finish());
        rg_switch.setOnCheckedChangeListener((group, checkedId) -> {
            if(rb_pwd.isChecked()){
                InputFilter.LengthFilter lengthFilter = new InputFilter.LengthFilter(20);
                //1. 清空
                et_top.setText("");
                //2. 设置输入类型
                et_top.setInputType(InputType.TYPE_CLASS_TEXT );
                // 3. 设置hint
                et_top.setHint("请输入账号");
                // 4. 设置过滤器
                et_top.setFilters(new InputFilter[]{lengthFilter});

                et_down.setText("");
                et_down.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                et_down.setHint("请输入密码");
                et_down.setFilters(new InputFilter[]{lengthFilter});

                tv_get.setText("忘记密码");
                et_top.requestFocus();
            }else if(rb_captcha.isChecked()){
                et_top.setText("");
                et_top.setInputType(InputType.TYPE_CLASS_PHONE);
                et_top.setHint("请输入手机号");
                et_top.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

                et_down.setText("");
                et_down.setInputType( InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                et_down.setHint("请输入验证码");
                et_down.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

                tv_get.setText("获取验证码");
                et_top.requestFocus();
            }
        });
        tv_get.setOnClickListener(v -> {
            if(rb_pwd.isChecked()){
                //跳转到修改密码界面
                Intent intent = new Intent(this, PasswordActivity.class);
                startActivity(intent);
            } else if (rb_captcha.isChecked()) {
                if(et_top.getText().toString().length()<11){
                    Toast.makeText(this,"请输入正确的手机号",Toast.LENGTH_SHORT).show();
                    return;
                }
                captcha = VerificationCodeGenerator.generateVerificationCode();
                // 创建 AlertDialog 的 Builder 对象
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // 设置对话框的标题
                builder.setTitle("验证码");
                // 设置对话框的消息内容
                builder.setMessage("还未开发，请勿登录");
                // 添加“确认”按钮并设置点击监听器
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“确认”后执行的操作
                    }
                });
                // 创建并显示对话框
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        tv_register.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
        bt_login.setOnClickListener(v -> {
            // 禁用登录按钮以防重复点击
            bt_login.setEnabled(false);
            //展示进度条
            login_progress.setVisibility(View.VISIBLE);
            String top = et_top.getText().toString();
            String down = et_down.getText().toString();
            if(rb_pwd.isChecked()){
                if(top.isEmpty()||down.isEmpty()) {
                    Toast.makeText(this,"账号或密码不能为空",Toast.LENGTH_SHORT).show();
                    login_progress.setVisibility(View.GONE);
                    bt_login.setEnabled(true);
                    return;}
                try {
                    JSONObject root = EmptyJSON.getForCommand();
                    JSONObject data = new JSONObject();
                    data.put("describe","UserLoginVerification");
                    data.put("account",top);
                    data.put("password",down);
                    root.put("data",data);
                    myService.sendStringToServer(root.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }else if (rb_captcha.isChecked()){
                if(down.length() != 6){
                    Toast.makeText(this,"请输入正确的验证码",Toast.LENGTH_SHORT).show();
                    login_progress.setVisibility(View.GONE);
                    bt_login.setEnabled(true);
                    return;
                }
                try {
                    JSONObject root = EmptyJSON.getForCommand();
                    JSONObject data = new JSONObject();
                    data.put("describe","UserLoginVerificationByCaptcha");
                    data.put("phone",top);
                    data.put("captcha",down);
                    root.put("data",data);
                    myService.sendStringToServer(root.toString());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        tv_manager.setOnClickListener(v -> {
            // 创建 AlertDialog 的 Builder 对象
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // 设置对话框的标题
            builder.setTitle("警告");
            // 设置对话框的消息内容
            builder.setMessage("你确定要以管理员身份登录吗？");
            // 添加“确认”按钮并设置点击监听器
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 调转到管理员界面
                }
            });
            // 添加“取消”按钮并设置点击监听器
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 点击“取消”后执行的操作
                    dialog.cancel(); // 只关闭对话框而不做其他操作
                }
            });

            // 创建并显示对话框
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 绑定服务
        Intent intent = new Intent(this, MyWebSocketService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

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

    // 保存登录状态和token
    public void saveLoginStatus(int userId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putInt("userId", userId);
        editor.apply();
    }

    public void EnterHomeActivity(int userId){
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("userId",userId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}