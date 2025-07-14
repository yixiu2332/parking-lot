package com.zyf.partinglot.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zyf.partinglot.activity.PasswordActivity;
import com.zyf.partinglot.R;

public class PasswordChangeFragment extends Fragment {

    private EditText et_account;
    private String account;
    private PasswordActivity context;

    public PasswordChangeFragment() {
    }
    public PasswordChangeFragment(String account) {
        this.account = account;
    }

    public PasswordChangeFragment(EditText et_account) {
        this.et_account = et_account;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (PasswordActivity)getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password_change, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText et_password1 = view.findViewById(R.id.et_password1);
        EditText et_password2 = view.findViewById(R.id.et_password2);
        Button bt_confirm = view.findViewById(R.id.bt_confirm);
        et_account = view.findViewById(R.id.et_account);
        bt_confirm.setOnClickListener(v -> {
            String password1 = et_password1.getText().toString();
            String password2 = et_password2.getText().toString();
            if(!password1.equals(password2)){
                if(password1.isEmpty()){
                    Toast.makeText(getContext(),"密码不能为空",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(),"两次密码不一样",Toast.LENGTH_SHORT).show();
                }
            }else{
                if(!context.helper.updatePasswordByAccount(account,password1)){
                    Toast.makeText(getContext(),"修改失败，请报告管理员",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(),"修改成功",Toast.LENGTH_SHORT).show();
                    context.finish();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        et_account.setText(account);
    }
}