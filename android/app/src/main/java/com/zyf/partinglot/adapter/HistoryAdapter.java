package com.zyf.partinglot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zyf.partinglot.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<String[]> data;

    public HistoryAdapter(List<String[]> data) {
        this.data = data;
    }
    public void updateData(List<String[]> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged(); // 刷新列表
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载布局文件
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_history_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 绑定数据
        String date = data.get(position)[0];
        String time = data.get(position)[1];
        String parking = data.get(position)[2];
        String space = data.get(position)[3];
        holder.history_date.setText(date);
        holder.history_time.setText(time);
        holder.history_parking.setText(parking);
        holder.history_space.setText(space);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // ViewHolder 类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView history_date;
        TextView history_time;
        TextView history_parking;
        TextView history_space;

        public ViewHolder(View itemView) {
            super(itemView);
            history_date = itemView.findViewById(R.id.history_date);
            history_time = itemView.findViewById(R.id.history_time);
            history_parking = itemView.findViewById(R.id.history_parking);
            history_space = itemView.findViewById(R.id.history_space);
        }
    }
}