package com.zyf.partinglot.fragment;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zyf.partinglot.activity.PasswordActivity;
import com.zyf.partinglot.R;
import com.zyf.partinglot.utils.VerificationCodeGenerator;

public class PasswordPhoneFragment extends Fragment {
    private String captcha;
    private PasswordActivity context;
    public PasswordPhoneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (PasswordActivity) getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_phone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText et_phone = view.findViewById(R.id.et_phone);
        EditText et_captcha = view.findViewById(R.id.et_captcha);
        Button bt_get = view.findViewById(R.id.bt_get);
        Button bt_submit = view.findViewById(R.id.bt_submit);
        bt_get.setOnClickListener(v -> {
            String phone = et_phone.getText().toString();
            if(phone.length()<11){
                Toast.makeText(getContext(),"请输入正确的手机号",Toast.LENGTH_SHORT).show();
                return;
            }
            if(!context.phoneVerify(phone)){
                Toast.makeText(getContext(),"手机号未注册",Toast.LENGTH_SHORT).show();
                return;
            }
            captcha = VerificationCodeGenerator.generateVerificationCode();
            // 创建 AlertDialog 的 Builder 对象
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            // 设置对话框的标题
            builder.setTitle("验证码");
            // 设置对话框的消息内容
            builder.setMessage("手机号"+et_phone.getText().toString()+"成功获取验证码"+ captcha +"。");
            // 添加“确认”按钮并设置点击监听器
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 点击“确认”后执行的操作
                }
            });
            // 创建并显示对话框
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
        bt_submit.setOnClickListener(v -> {
            String phone = et_phone.getText().toString();
            if(!et_captcha.getText().toString().equals(captcha)||captcha.isEmpty()){
                Toast.makeText(getContext(),"请输入正确的验证码",Toast.LENGTH_SHORT).show();
            }else{
                context.startFragment2(context.getAccountByPhone(phone));
            }
        });
    }
}