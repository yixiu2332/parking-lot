package com.zyf.partinglot.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.CountDownTimer;
import java.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zyf.partinglot.activity.HomeActivity;
import com.zyf.partinglot.message.MyViewModel;
import com.zyf.partinglot.R;
import com.zyf.partinglot.utils.EmptyJSON;

import org.json.JSONException;
import org.json.JSONObject;


public class ControlFragment extends Fragment implements HomeActivity.HomeToControl {
    private final String TAG = "ControlFragment";
    private MyViewModel myViewModel;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 600000; // 10分钟
    private final long startTimeInMillis = 600000; // 10分钟
    private boolean clockIsRunning = false;
    private TextView tv_select_id;
    private TextView tv_clock;
    private Button bt_cancel;
    private CheckBox cb_switch;
    private View frameLayout;

    private HomeActivity homeActivity;
    private Button bt_startOrEnd;
    private TextView tv_startTime;
    private ImageView imageView;

    public ControlFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeActivity = (HomeActivity) getActivity();
        homeActivity.registerHomeToControl(this);
        myViewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        myViewModel.getStartTime().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                setStartTime();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get controls
        frameLayout = view.findViewById(R.id.frameLayout);
        tv_select_id = view.findViewById(R.id.tv_select_id);
        tv_clock = view.findViewById(R.id.tv_clock);
        bt_cancel = view.findViewById(R.id.bt_cancel);
        cb_switch = view.findViewById(R.id.cb_switch);
        bt_startOrEnd = view.findViewById(R.id.button);
        tv_startTime = view.findViewById(R.id.tv_startTime);
        imageView = view.findViewById(R.id.imageView);

        /*  Set Listeners  */
        bt_startOrEnd.setOnClickListener(v -> {
            if(myViewModel.getStartOrEnd().getValue()){
                //End the timer
                bt_startOrEnd.setText("开始计时");
                tv_select_id.setText("无");
                myViewModel.setStartOrEnd(false);
                setStartTime();
                myViewModel.setCostMoney(homeActivity.getCostMoney());
                sendAEndTimingSignal();
                homeActivity.ViewPager2Turn(2);
            } else {
                //Start the timer
                CancelAppointmentClockAfterStartTime();
                bt_startOrEnd.setText("结束计时");
                myViewModel.setStartOrEnd(true);
                myViewModel.setStartTime(0l);
                setStartTime();
                sendAStartTimingSignal();
            }
        });

        bt_cancel.setOnClickListener(v -> {
            CancelYourAppointment();
            homeActivity.ViewPager2Turn(0);
        });

        cb_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HomeActivity activity = (HomeActivity) getActivity();
                if(isChecked){
                    activity.ParkingLockControl("unlock");
                }else {
                    activity.ParkingLockControl("locked");
                }
            }
        });
    }

    private void sendAStartTimingSignal() {
        try {
            JSONObject command = EmptyJSON.getForCommand();
            JSONObject data = new JSONObject();
            data.put("describe","StartTimingSignal");
            data.put("userId",homeActivity.userId);
            data.put("spaceId",myViewModel.getReservedSpaceNumber().getValue());
            command.put("data",data);
            homeActivity.myService.sendStringToServer(command.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAEndTimingSignal() {
        try {
            JSONObject command = EmptyJSON.getForCommand();
            JSONObject data = new JSONObject();
            data.put("describe","EndTimingSignal");
            data.put("money",myViewModel.getCostMoney().getValue());
            data.put("userId",homeActivity.userId);
            data.put("spaceId",myViewModel.getReservedSpaceNumber().getValue());
            command.put("data",data);
            homeActivity.myService.sendStringToServer(command.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        // Get the value of reservedSpaceNumber
        Integer reservedSpaceNumber = myViewModel.getReservedSpaceNumber().getValue();

        if (reservedSpaceNumber.intValue() == 0) {
            tv_startTime.setVisibility(View.INVISIBLE);
            tv_clock.setVisibility(View.INVISIBLE);
            frameLayout.setVisibility(View.VISIBLE);
            bt_cancel.setEnabled(false);
        }
        else
        {
            tv_select_id.setText("停车位"+myViewModel.getReservedSpaceNumber().getValue());
            frameLayout.setVisibility(View.GONE);
            if(myViewModel.getStartOrEnd().getValue()){
                bt_cancel.setEnabled(false);
                bt_startOrEnd.setText("结束计时");
                setStartTime();
            }
            else
            {
                bt_cancel.setEnabled(true);
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sp_ControlFragment", Context.MODE_PRIVATE);
                if(sharedPreferences.getBoolean("clockIsRunning",false)){
                    long currentTime = sharedPreferences.getLong("currentTime", 0);
                    long clockTime = sharedPreferences.getLong("clockTime", 0);
                    long nowTime = System.currentTimeMillis();
                    if(nowTime - currentTime>clockTime){
                        //执行计时结束逻辑
                        CancelYourAppointment();
                    }else{

                        timeLeftInMillis = clockTime - (nowTime-currentTime);
                        startCountDown();
                    }
                }else startCountDown();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeActivity.unregisterHomeToControl(this);
        if(clockIsRunning){
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sp_ControlFragment", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("clockIsRunning",true);
            editor.putLong("currentTime",System.currentTimeMillis());
            editor.putLong("clockTime",timeLeftInMillis);
            editor.apply();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void setStartTime(){
        tv_startTime.setVisibility(View.VISIBLE);
        if(myViewModel.getStartOrEnd().getValue()&&myViewModel.getReservedSpaceNumber().getValue()!=0){
        int hour = (int) (myViewModel.getStartTime().getValue() / 3600000) ;
        int min = (int) (myViewModel.getStartTime().getValue() /60000) % 60;
        String timeFormatted = String.format("%02d:%02d", hour, min);
        tv_startTime.setText(timeFormatted);}
        else{
            tv_startTime.setText("00:00");
            tv_startTime.setVisibility(View.INVISIBLE);
        }
    }
    private void startCountDown() {
        tv_clock.setVisibility(View.VISIBLE);
        clockIsRunning =true;
        // 确保取消旧计时器
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        // 创建新的计时器
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished; // 更新剩余时间
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                CancelYourAppointment();
                homeActivity.ViewPager2Turn(0);
            }
        }.start();
    }

    private void CancelAppointmentClockAfterStartTime(){
        tv_clock.setVisibility(View.INVISIBLE);
        clockIsRunning = false;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sp_ControlFragment", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("clockIsRunning",false);
        editor.apply();
        if(countDownTimer!=null) countDownTimer.cancel();
        // 重置剩余时间为初始值
        timeLeftInMillis = startTimeInMillis;
        bt_cancel.setEnabled(false);
        tv_clock.setText("00:00");
    }
    private void CancelYourAppointment(){
        tv_clock.setVisibility(View.INVISIBLE);
        clockIsRunning =false;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("sp_ControlFragment", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("clockIsRunning",false);
        editor.apply();
        if(countDownTimer!=null) countDownTimer.cancel();
        // 重置剩余时间为初始值
        timeLeftInMillis = startTimeInMillis;
        homeActivity.CancelAppointment(String.valueOf(myViewModel.getReservedSpaceNumber().getValue()));
        myViewModel.setReservedSpaceNumber(0);
        homeActivity.runOnUiThread(() -> {
            bt_cancel.setEnabled(false);
            tv_clock.setText("00:00");
            tv_select_id.setText("无");
        });
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        tv_clock.setText(timeFormatted);
    }
    @Override
    public void sendDataToFragment(String message) {
        byte[] decode = Base64.getDecoder().decode(message);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        imageView.setImageBitmap(bitmap);
    }
}