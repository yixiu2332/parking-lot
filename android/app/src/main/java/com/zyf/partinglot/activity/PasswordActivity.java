package com.zyf.partinglot.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zyf.partinglot.R;
import com.zyf.partinglot.dao.UserDBHelper;
import com.zyf.partinglot.fragment.PasswordChangeFragment;
import com.zyf.partinglot.fragment.PasswordPhoneFragment;

public class PasswordActivity extends AppCompatActivity {

    public UserDBHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        ImageButton ibt_back = findViewById(R.id.ibt_back);
        ibt_back.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        startFragment1();
        helper = UserDBHelper.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.closeDB();
    }

    public boolean phoneVerify(String phone){
        Cursor cursor = helper.findUserByPhone(phone);
        int len = cursor.getCount();
        cursor.close();
        return len>0;
    }
    public void startFragment1(){
        // 使用 getSupportFragmentManager() 获取支持库版本的 FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        PasswordPhoneFragment fragment = new PasswordPhoneFragment();
        // 将新 Fragment 添加到由 R.id.container 指定的容器中
        transaction.replace(R.id.frame,fragment,null)
                .commit();
    }

    public String getAccountByPhone(String phone){
        Cursor cursor = helper.findUserByPhone(phone);
        String str = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) { // 将光标移动到第一行
                    // 假设account是你要找的列名
                     str = cursor.getString(cursor.getColumnIndexOrThrow("account"));
                }
            } finally {
                cursor.close(); // 记得关闭游标以避免资源泄漏
            }
        }
        return str;
    }
    public void startFragment2(String account){
        // 使用 getSupportFragmentManager() 获取支持库版本的 FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        PasswordChangeFragment fragment = new PasswordChangeFragment(account);
        // 将新 Fragment 添加到由 R.id.container 指定的容器中
        transaction.replace(R.id.frame,fragment,null)
                .commit();
    }
}