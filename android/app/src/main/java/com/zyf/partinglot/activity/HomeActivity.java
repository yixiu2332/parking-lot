package com.zyf.partinglot.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zyf.partinglot.R;
import com.zyf.partinglot.dao.User;
import com.zyf.partinglot.adapter.MyPagerAdapter;
import com.zyf.partinglot.adapter.MyPopupAdapter;
import com.zyf.partinglot.fragment.UserFragment;
import com.zyf.partinglot.message.MyViewModel;
import com.zyf.partinglot.message.MyWebSocketService;
import com.zyf.partinglot.utils.EmptyJSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity implements MyWebSocketService.MyCallback {

    private final String TAG = "HomeActivity";
    public MyViewModel myViewModel;
    public boolean loading = true;
    public int userId;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private ViewPager2 viewPager;
    private MyPagerAdapter adapter;
    private TabLayout tabLayout;
    private TextView tv_home;

    public MyWebSocketService myService;
    private boolean isBound = false;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MyWebSocketService.LocalBinder binder = (MyWebSocketService.LocalBinder) service;
            myService = binder.getService();
            isBound = true;
            myService.registerCallback(HomeActivity.this); // 注册回调
            //必须放在这里，因为绑定服务有延迟
            //获取最新数据
                try {
                    JSONObject root = EmptyJSON.getForCommand();
                    JSONObject data = new JSONObject();
                    data.put("describe","HomeRequestData");
                    data.put("userId",userId);
                    root.put("data",data);
                    executor.submit(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        myService.sendStringToServer(root.toString());
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };
    public PopupWindow popupWindow;
    private ProgressBar progress;
    private RecyclerView recyclerView;
    private View popupView;
    private Timer timer;
    private CheckBox cb_switch;

    @Override
    public void onDataReceived(String message) {
        runOnUiThread(() -> {
            try {
                JSONObject root = new JSONObject(message);
                JSONObject data = root.getJSONObject("data");
                switch (data.getString("describe")){
                    case "ov7670":
                        if(cb_switch.isChecked()){
                            controlFragment.sendDataToFragment(data.getString("image"));
                        }
                        break;
                    case "HomeRequestData":
                        loading = false;
                        JSONObject parkingLot = data.getJSONObject("parkingLot");
                        myViewModel.setRate1(parkingLot.getString("rate1"));
                        myViewModel.setRate2(parkingLot.getString("rate2"));
                        myViewModel.setTotalSpaces(parkingLot.getString("totalSpaces"));
                        myViewModel.setAvailableSpaces(parkingLot.getString("availableSpaces"));
                        JSONObject user ;
                        if( ( user = data.optJSONObject("user") )!=null){
                            myViewModel.setReservedSpaceNumber(user.getInt("spaceId"));
                            myViewModel.setCostMoney(user.getDouble("money"));
                            if(user.getBoolean("start")){
                                myViewModel.setStartOrEnd(true);
                                myViewModel.setStartTime(System.currentTimeMillis()-user.getLong("startTime"));
                            }

                            if(user.optInt("spaceId") == 0){
                                if(homeToInformation!=null)
                                    homeToInformation.sendDataToFragment("Button_True");
                            }
                        }
                        progress.setVisibility(View.GONE);

                        break;
                    case "ReserveAParkingSpace":
                        if(!data.getString("reply").equals("yes")){
                            showToast("车位已被占用，请重新预约！");
                            homeToInformation.sendDataToFragment("Button_False");
                        }else{
                            //更新预约停车位
                            myViewModel.setReservedSpaceNumber(Integer.parseInt(data.getString("id")));
                            showToast("预约成功");
                            homeToInformation.sendDataToFragment("Button_False");
                        }
                        break;
                    case "CheckParkingSpace":
                        Map<String,String> map = new HashMap<>();
                        JSONObject space = data.getJSONObject("parkingSpace");
                        String id;
                        for(int i=0;i<Integer.parseInt(myViewModel.getTotalSpaces().getValue());i++){
                            id = String.valueOf(i+1);
                            map.put(id,space.getString(id));
                        }
                        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 设置布局管理器
                        MyPopupAdapter popupAdapter = new MyPopupAdapter(HomeActivity.this,map); // 设置适配器
                        recyclerView.setAdapter(popupAdapter); // 将适配器绑定到 RecyclerView}
                        shutDownPopupBarrier(popupView);
                        break;
                    case "QueryUserInformation":
                        executor.submit(() -> {
                            User user1 = new User();
                            try {
                                JSONObject userJSON = data.getJSONObject("user");
                                user1.setName(userJSON.getString("name"));
                                user1.setAccount(userJSON.getString("account"));
                                user1.setPhone(userJSON.getString("phone"));
                                user1.setLicensePlate(userJSON.optString("licensePlate"));
                                user1.setImage(Base64.getDecoder().decode(userJSON.getString("image")));
                                homeToUser.sendDataToFragment(user1);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break;

                    case "StartTimingSignal":
                        break;
                    case "EndTimingSignal":
                        if(data.getString("reply").equals("ok")){
                            myViewModel.setReservedSpaceNumber(0);
                        }
                        break;
                        default:
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Intent receivedIntent = getIntent();
        if (receivedIntent == null) {
            finish();
        }else{
            userId = receivedIntent.getIntExtra("userId",0);
        }
        progress = findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        // 启动WebSocket服务
        Intent intent = new Intent(this, MyWebSocketService.class);
        startService(intent);
        bindService(intent,connection, Context.BIND_AUTO_CREATE);
        // 获取 ViewModel 实例
        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        // 设置 LiveData 的值
        myViewModel.setRate1("0");
        myViewModel.setRate2("0");
        myViewModel.setTotalSpaces("0");
        myViewModel.setAvailableSpaces("0");
        myViewModel.setReservedSpaceNumber(Integer.valueOf(0));
        myViewModel.setStartTime(0l);
        myViewModel.setStartOrEnd(false);
        myViewModel.setCostMoney(0.0);

        cb_switch = findViewById(R.id.cb_switch);
        tv_home = findViewById(R.id.tv_home);
        tabLayout = findViewById(R.id.tabLayout);
        // 初始化 ViewPager2 和适配器
        viewPager = findViewById(R.id.viewPager);
        Bundle bundle = new Bundle();
        bundle.putString("total","0");
        bundle.putString("idle","0");
        bundle.putString("toll1","0");
        bundle.putString("toll2","0");
        bundle.putInt("select_id",0);
        adapter = new MyPagerAdapter(this,bundle);
        viewPager.setAdapter(adapter);

        // 使用 TabLayoutMediator 绑定 TabLayout 和 ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // 根据位置设置 Tab 的标题
            View customTabView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            ImageView imageView = customTabView.findViewById(R.id.image);

            switch (position) {
                case 0:
                    imageView.setImageResource(R.drawable.icon_home_check);
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.icon_control_uncheck);
                    break;
                case 2:
                    imageView.setImageResource(R.drawable.icon_pay_uncheck);
                    break;
                case 3:
                    imageView.setImageResource(R.drawable.icon_user_uncheck);
                    break;
                default:
            }
            // 设置自定义视图到 Tab
            tab.setCustomView(customTabView);
        }).attach();

// 监听 Tab 的选中状态变化，动态更新选中图标
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 获取当前选中的 Tab，更新图标
                View customTabView = tab.getCustomView();
                if (customTabView != null) {
                    ImageView imageView = customTabView.findViewById(R.id.image);
                    if (imageView != null) {
                        // 根据 Tab 的位置更新选中的图标
                        switch (tab.getPosition()) {
                            case 0:
                                imageView.setImageResource(R.drawable.icon_home_check); // 选中
                                tv_home.setText("首页");
                                break;
                            case 1:
                                imageView.setImageResource(R.drawable.icon_control_check); // 选中
                                tv_home.setText("控制车位");
                                break;
                            case 2:
                                imageView.setImageResource(R.drawable.icon_pay_check); // 选中
                                tv_home.setText("缴费");
                                break;
                            case 3:
                                imageView.setImageResource(R.drawable.icon_user_check); // 选中
                                tv_home.setText("个人主页");
                                break;
                            default:

                        }
                    }
                    // 点击 Tab 时直接跳转，不带滑动动画
                    viewPager.setCurrentItem(tab.getPosition(), false);  // 第二个参数设置为 false 禁用滑动动画
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 获取当前未选中的 Tab，更新图标
                View customTabView = tab.getCustomView();
                if (customTabView != null) {
                    ImageView imageView = customTabView.findViewById(R.id.image);
                    if (imageView != null) {
                        // 设置未选中的图标
                        switch (tab.getPosition()) {
                            case 0:
                                imageView.setImageResource(R.drawable.icon_home_uncheck); // 选中
                                break;
                            case 1:
                                imageView.setImageResource(R.drawable.icon_control_uncheck); // 选中
                                break;
                            case 2:
                                imageView.setImageResource(R.drawable.icon_pay_uncheck); // 选中
                                break;
                            case 3:
                                imageView.setImageResource(R.drawable.icon_user_uncheck); // 选中
                                break;
                            default:
                        }
                    }
                }
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 如果 Tab 被重新选中，重新设置选中图标
                onTabSelected(tab);
            }
        });
    }

    public ActivityResultLauncher<Intent> activityResultLauncher;
    @Override
    protected void onStart() {
        super.onStart();
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            int resultId = data.getIntExtra("resultId", 0);
                            // 处理返回的数据
                            Log.d(TAG, "onStart: "+resultId);
                            if(resultId == 1) homeToUser.RequestUseInformation();
                        }
                    }
                });

        //定时任务,计算停车时间
        timer = new Timer();
        // 定义一个任务
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    myViewModel.setStartTime(myViewModel.getStartTime().getValue()+60000l);
                });
            }
        };
        timer.schedule(task, 60000, 60000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        if (isBound) {
            myService.unregisterCallback(this); // 取消注册回调
            unbindService(connection);
            isBound = false;
        }
        Intent intent = new Intent(this,MyWebSocketService.class);
        stopService(intent);
        if(executor != null) executor.shutdown();
    }

    public int getDataSelectId(){
        LiveData<Integer> dataOfValue5 = myViewModel.getReservedSpaceNumber();
        if(dataOfValue5.getValue()!=null){
            return dataOfValue5.getValue();
        }
        return 0;
    }

    public void showPopupWindow(View anchorView) {
        anchorView.setEnabled(true);
        // 加载 PopupWindow 的内容布局
        popupView = getLayoutInflater().inflate(R.layout.popup_window, null);
        recyclerView = popupView.findViewById(R.id.recyclerView);
        ProgressBar popup_progress = popupView.findViewById(R.id.popup_progress);

        // 创建 PopupWindow 实例
        // 最后一个参数表示是否可聚焦
        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setHeight(1200);
        // 设置背景 Drawable，如果不设置，PopupWindow 可能不会响应触摸事件
        // 自定义颜色，其中FF表示不透明度(A)，AA, BB, CC分别代表红(R)、绿(G)、蓝(B)的值
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xffd4237a));
        // 如果点击外部区域可以关闭 PopupWindow
        popupWindow.setOutsideTouchable(true);
        View viewById = popupView.findViewById(R.id.frameLayout);
        viewById.setVisibility(View.VISIBLE);
        popup_progress.setVisibility(View.VISIBLE);
        // 显示 PopupWindow
        // 第一个参数是用于确定 PopupWindow 位置的父视图
        // 第二个和第三个参数分别是 x 和 y 方向的偏移量
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
        anchorView.setEnabled(true);
    }

    private void shutDownPopupBarrier(View view){
        view.findViewById(R.id.popup_progress).setVisibility(View.GONE);
        view.findViewById(R.id.frameLayout).setVisibility(View.GONE);

    }

    public void reserveAParkingSpace(String id){
        try {
            JSONObject command = EmptyJSON.getForCommand();
            JSONObject data = new JSONObject();
            data.put("describe","ReserveAParkingSpace");
            data.put("id",id);
            data.put("state","1");
            data.put("userId",userId);
            command.put("data",data);
            myService.sendStringToServer(command.toString());

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public void ParkingLockControl(String state){
        JSONObject command;
        JSONObject data;
        try {
            command = EmptyJSON.getForCommand();
            data = new JSONObject();
            data.put("describe","ParkingLockControl");
            data.put("spaceId",String.valueOf(myViewModel.getReservedSpaceNumber().getValue()));
            data.put("lockState",state);
            command.put("data",data);
            myService.sendStringToServer(command.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public void CancelAppointment(String id){
        try {
            JSONObject command = EmptyJSON.getForCommand();
            JSONObject data = new JSONObject();
            data.put("describe","CancelAppointment");
            data.put("spaceId",id);
            data.put("userId",userId);
            command.put("data",data);
            myService.sendStringToServer(command.toString());
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void checkParkingSpace() {
        executor.submit(() -> {
            try {
                JSONObject root = EmptyJSON.getForCommand();
                JSONObject data = new JSONObject();
                data.put("describe","CheckParkingSpace");
                root.put("data",data);
                myService.sendStringToServer(root.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    public void ViewPager2Turn(int position){
        Log.d(TAG, "ViewPager2Turn: "+ position);
        viewPager.setCurrentItem(position, false);
    }

    public void sendPaySuccessToServer() {
        try {
            JSONObject command = EmptyJSON.getForCommand();
            JSONObject data = new JSONObject();
            data.put("describe","PaySuccess");
            data.put("userId",userId);
            command.put("data",data);
            myService.sendStringToServer(command.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public double getCostMoney(){
        long min = myViewModel.getStartTime().getValue() / 60000;
        if(min <= 60){
            return min/15 * Double.parseDouble(myViewModel.getRate1().getValue());
        }else{
            return 4*Double.parseDouble(myViewModel.getRate1().getValue())
                    + (min-60)/15 * Double.parseDouble(myViewModel.getRate2().getValue());
        }
    }

    //定义回调接口
    public interface HomeToControl<Fragment>{
        public void sendDataToFragment(String message);
    }
    //注册回调
    private HomeToControl controlFragment;
    public void registerHomeToControl(HomeToControl fragment){
        controlFragment = fragment;
    }
    //取消注册
    public void unregisterHomeToControl(HomeToControl fragment) {
        controlFragment =null;
    }

    //定义回调接口
    public interface HomeToInformation<Fragment>{
        public void sendDataToFragment(String message);
    }
    //注册回调
    private HomeToInformation homeToInformation;
    public void registerHomeToInformation(HomeToInformation fragment){
        homeToInformation = fragment;
    }
    //取消注册
    public void unregisterHomeToInformation(HomeToInformation fragment) {
        homeToInformation =null;
    }

    //定义回调接口
    public interface HomeToUser<Fragment>{
        public void sendDataToFragment(User user);
    }
    //注册回调
    private UserFragment homeToUser;
    public void registerHomeToUser(HomeToUser fragment){
        homeToUser = (UserFragment) fragment;
    }
    //取消注册
    public void unregisterHomeToUser(HomeToUser fragment) {
        homeToUser =null;
    }

    //定义回调接口
    public interface HomeToPay<Fragment>{
        public void sendDataToFragment(String message);
    }
    //注册回调
    private HomeToPay homeToPay;
    public void registerHomeToPay(HomeToPay fragment){
        homeToPay = fragment;
    }
    //取消注册
    public void unregisterHomeToPay(HomeToPay fragment) {
        homeToPay =null;
    }
}