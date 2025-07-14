package com.zyf.partinglot.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zyf.partinglot.R;
import com.zyf.partinglot.activity.HistoryActivity;
import com.zyf.partinglot.activity.HomeActivity;
import com.zyf.partinglot.activity.PasswordActivity;
import com.zyf.partinglot.message.MyViewModel;


public class PayFragment extends Fragment implements HomeActivity.HomeToPay {

    private HomeActivity homeActivity;
    private Button bt_pay;
    private TextView tv_cost;

    private MyViewModel myViewModel;

    public PayFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeActivity = (HomeActivity) getActivity();
        // 获取 ViewModel 实例
        myViewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pay, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_cost = view.findViewById(R.id.tv_cost);
        bt_pay = view.findViewById(R.id.bt_pay);
        bt_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myViewModel.setCostMoney(0.0);
                showToastSuccess("缴费成功");
                bt_pay.setEnabled(false);
                tv_cost.setText("0元");
                homeActivity.sendPaySuccessToServer();
            }
        });
        TextView tv_history = view.findViewById(R.id.tv_history);
        tv_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(homeActivity, HistoryActivity.class);
                intent.putExtra("userId",homeActivity.userId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateUI();
    }

    private void UpdateUI() {
        if(myViewModel.getCostMoney().getValue() <= 1){
            tv_cost.setText("0元");
            bt_pay.setEnabled(false);
        }else {
            tv_cost.setText(myViewModel.getCostMoney().getValue()+"元");
            bt_pay.setEnabled(true);
        }
    }

    private Toast currentToast;
    private void showToastWarn(String s){
        if(currentToast!=null){
            currentToast.cancel();
        }
        View layout = getLayoutInflater().inflate(R.layout.custom_toast_warn,null);
        TextView text = layout.findViewById(R.id.text);
        text.setText(s);
        currentToast = new Toast(getContext());
        currentToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0); // 设置显示位置
        currentToast.setDuration(Toast.LENGTH_SHORT);
        currentToast.setView(layout); // 设置自定义视图
        currentToast.show();
    }
    private void showToastSuccess(String s){
        if(currentToast!=null){
            currentToast.cancel();
        }
        View layout = getLayoutInflater().inflate(R.layout.custom_toast_success,null);
        TextView text = layout.findViewById(R.id.text);
        text.setText(s);
        currentToast = new Toast(getContext());
        currentToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0); // 设置显示位置
        currentToast.setDuration(Toast.LENGTH_SHORT);
        currentToast.setView(layout); // 设置自定义视图
        currentToast.show();
    }


    @Override
    public void sendDataToFragment(String message) {

    }
}