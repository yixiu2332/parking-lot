package com.zyf.partinglot.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zyf.partinglot.activity.HomeActivity;
import com.zyf.partinglot.message.MyViewModel;
import com.zyf.partinglot.R;

public class InformationFragment extends Fragment implements HomeActivity.HomeToInformation {

    private MyViewModel myViewModel;
    private TextView tv_toll1;
    private TextView tv_toll2;
    private TextView tv_total;
    private TextView tv_idle;

    private Button bt_reserve;

    private HomeActivity homeActivity;

    public InformationFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeActivity = (HomeActivity) getActivity();
        homeActivity.registerHomeToInformation(this);

        // 获取 ViewModel 实例
        myViewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_toll1 = view.findViewById(R.id.tv_toll1);

        tv_toll2 = view.findViewById(R.id.tv_toll2);

        tv_total = view.findViewById(R.id.tv_total);

        tv_idle = view.findViewById(R.id.tv_idle);


        // 观察 LiveData 数据变化
        myViewModel.getRate1().observe(getViewLifecycleOwner(), value -> {
            tv_toll1.setText(value);
        });
        myViewModel.getRate2().observe(getViewLifecycleOwner(), value -> {
            tv_toll2.setText(value);
        });
        myViewModel.getTotalSpaces().observe(getViewLifecycleOwner(), value -> {
            tv_total.setText(value);
        });
        myViewModel.getAvailableSpaces().observe(getViewLifecycleOwner(), value -> {
            tv_idle.setText(value);
        });

        bt_reserve = view.findViewById(R.id.bt_reserve);
        bt_reserve.setEnabled(false);
        bt_reserve.setOnClickListener(v -> {
            if(myViewModel.getCostMoney().getValue()>=1){
                Toast.makeText(getContext(),"请先完成缴费",Toast.LENGTH_SHORT).show();
                return;
            }
            v.setEnabled(false);
            //Request modification
            homeActivity.checkParkingSpace();
            homeActivity.showPopupWindow(v);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
    private void updateUI() {
        // 更新UI逻辑
        Integer reservedSpaceNumber = myViewModel.getReservedSpaceNumber().getValue();
        if (reservedSpaceNumber == null || reservedSpaceNumber == 0) {
            if(homeActivity.loading){
                bt_reserve.setEnabled(false);
            }else {
                bt_reserve.setEnabled(true);
            }
        }else {
            bt_reserve.setEnabled(false);
        }
        tv_toll1.setText(myViewModel.getRate1().getValue());
        tv_toll2.setText(myViewModel.getRate2().getValue());
        tv_total.setText(myViewModel.getTotalSpaces().getValue());
        tv_idle.setText(myViewModel.getAvailableSpaces().getValue());

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        homeActivity.unregisterHomeToInformation(this);
    }

    @Override
    public void sendDataToFragment(String message) {
        switch (message){
            case "Button_False":
                bt_reserve.setEnabled(false);
                break;
            case "Button_True":
                bt_reserve.setEnabled(true);
                break;
            default:
        }
    }
}