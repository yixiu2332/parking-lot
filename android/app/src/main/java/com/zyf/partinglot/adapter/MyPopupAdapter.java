package com.zyf.partinglot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zyf.partinglot.activity.HomeActivity;
import com.zyf.partinglot.R;

import java.util.Map;

public class MyPopupAdapter extends RecyclerView.Adapter<MyPopupAdapter.ViewHolder> {
    private Map<String,String> map;
    private HomeActivity activity;
    public MyPopupAdapter(Context context , Map<String,String> map) {
        this.map = map;
        this.activity = (HomeActivity)context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.popup_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //map是顺序键值对
        String state = map.get(String.valueOf(position+1));
        holder.textView.setText("停车位"+ (position + 1));
        if(!state.equals("0")){
            holder.button.setEnabled(false);
        }
        // 设置按钮点击事件
        holder.button.setOnClickListener(v -> {
            // 处理按钮点击
            activity.reserveAParkingSpace(String.valueOf(position+1));
            activity.popupWindow.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        return map.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            button = itemView.findViewById(R.id.button);
        }
    }
}