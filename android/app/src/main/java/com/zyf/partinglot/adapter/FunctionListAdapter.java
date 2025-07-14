package com.zyf.partinglot.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zyf.partinglot.activity.HomeActivity;
import com.zyf.partinglot.R;
import com.zyf.partinglot.fragment.UserFragment;

import java.util.List;

public class FunctionListAdapter extends RecyclerView.Adapter<FunctionListAdapter.FunctionViewHolder> {

    private final List<FunctionItem> functionItems;
    private HomeActivity homeActivity;
    private UserFragment userFragment;
    // 构造函数
    public FunctionListAdapter(HomeActivity homeActivity,UserFragment userFragment,List<FunctionItem> functionItems) {
        this.functionItems = functionItems;
        this.homeActivity = homeActivity;
        this.userFragment = userFragment;
    }

    @NonNull
    @Override
    public FunctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载功能项布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_function, parent, false);
        return new FunctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FunctionViewHolder holder, int position) {
        // 获取当前功能项
        FunctionItem item = functionItems.get(position);
        switch (position){
            case 0:
                holder.itemView.findViewById(R.id.item_function_root).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPreferences = homeActivity.getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("isLoggedIn", false);
                        editor.commit();
                        homeActivity.finish();
                    }
                });
                break;

            default:
                holder.itemView.findViewById(R.id.item_function_root).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(userFragment.currentToast!=null){
                            userFragment.currentToast.cancel();
                        }
                        LayoutInflater inflater = userFragment.getLayoutInflater();
                        View layout = inflater.inflate(R.layout.custom_toast_warn,null);
                        TextView text = layout.findViewById(R.id.text);
                        text.setText("暂未开发");
                        userFragment.currentToast = new Toast(userFragment.getContext());
                        Toast toast = userFragment.currentToast;
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0); // 设置显示位置
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setView(layout); // 设置自定义视图
                        toast.show();}
                    });
        }

        // 设置图标和标题
        holder.icon.setImageResource(item.getIconResId());
        holder.title.setText(item.getTitle());
    }

    @Override
    public int getItemCount() {
        return functionItems.size();
    }

    // ViewHolder 类
    static class FunctionViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public FunctionViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.functionIcon);
            title = itemView.findViewById(R.id.functionTitle);
        }
    }
}
