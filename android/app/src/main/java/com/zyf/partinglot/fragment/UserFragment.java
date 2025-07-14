package com.zyf.partinglot.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zyf.partinglot.activity.HomeActivity;
import com.zyf.partinglot.R;
import com.zyf.partinglot.activity.UpdateUserActivity;
import com.zyf.partinglot.dao.User;
import com.zyf.partinglot.adapter.FunctionItem;
import com.zyf.partinglot.adapter.FunctionListAdapter;
import com.zyf.partinglot.utils.EmptyJSON;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class UserFragment extends Fragment implements HomeActivity.HomeToUser {
    private final String TAG = "UserFragment";
    public Toast currentToast; // 全局变量
    private RecyclerView functionList;
    private HomeActivity homeActivity;
    private FunctionListAdapter adapter;
    private User user;
    private Bitmap bitmap;
    private ImageView userAvatar;
    private TextView userNickname;
    private TextView userAccount;
    private TextView license_plate;
    private TextView update_text;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeActivity = (HomeActivity) getActivity();
        homeActivity.registerHomeToUser(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载布局
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化用户信息视图
        userAvatar = view.findViewById(R.id.image);//image
        userNickname = view.findViewById(R.id.userNickname);
        userAccount = view.findViewById(R.id.userAccount);
        license_plate = view.findViewById(R.id.license_plate);
        update_text = view.findViewById(R.id.update_text);

        // 设置用户信息（示例数据）

        userNickname.setText("昵称：");
        userAccount.setText("账号：");
        license_plate.setText("车牌号：");
        userAvatar.setImageResource(R.drawable.icon_user_avatar);

        update_text.setOnClickListener(v -> {
            //跳转到
            Intent intent = new Intent(homeActivity, UpdateUserActivity.class);
            intent.putExtra("userId",homeActivity.userId);
            intent.putExtra("userName",user.getName());
            intent.putExtra("image", Base64.getEncoder().encodeToString(user.getImage()));
            intent.putExtra("licensePlate",user.getLicensePlate());
            homeActivity.activityResultLauncher.launch(intent);
        });

        // 初始化功能列表
        functionList = view.findViewById(R.id.functionList);
        functionList.setLayoutManager(new LinearLayoutManager(getContext()));

        // 设置适配器
        List<FunctionItem> functionItems = new ArrayList<>();
        //添加更多列表项

        functionItems.add(new FunctionItem(R.drawable.icon_logout, "退出登陆"));
        functionItems.add(new FunctionItem(R.drawable.icon_about, "关于"));
        functionItems.add(new FunctionItem(R.drawable.icon_settings, "设置"));

        adapter = new FunctionListAdapter(homeActivity,this,functionItems);
        functionList.setAdapter(adapter);

        //向服务器请求用户信息
        RequestUseInformation();

    }

    public void RequestUseInformation() {
        try {
            JSONObject command = EmptyJSON.getForCommand();
            JSONObject data = new JSONObject();
            data.put("describe","QueryUserInformation");
            data.put("userId",homeActivity.userId);
            command.put("data",data);
            homeActivity.myService.sendStringToServer(command.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeActivity.unregisterHomeToUser(this);
    }
    private void LoadUserToView(User user){
        homeActivity.runOnUiThread(() -> {
            String name = user.getName();
            String account = user.getAccount();
            String licensePlate = user.getLicensePlate();
            byte[] image = user.getImage();
            // 将byte数组转换为Bitmap
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            userNickname.setText("昵称："+name);
            userAccount.setText("账号："+account);
            license_plate.setText("车牌号："+licensePlate);
            userAvatar.setImageBitmap(bitmap);
        });

    }
    @Override
    public void sendDataToFragment(User user) {
        if(user!=null){
            this.user = user;
            LoadUserToView(user);
        }
    }
}