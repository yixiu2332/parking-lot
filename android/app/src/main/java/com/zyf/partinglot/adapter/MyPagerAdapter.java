package com.zyf.partinglot.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zyf.partinglot.activity.HomeActivity;
import com.zyf.partinglot.fragment.ControlFragment;
import com.zyf.partinglot.fragment.InformationFragment;
import com.zyf.partinglot.fragment.PayFragment;
import com.zyf.partinglot.fragment.UserFragment;

public class MyPagerAdapter extends FragmentStateAdapter {
    private final String total;
    private final String idle;
    private final String toll1;
    private final String toll2;

    private final HomeActivity homeActivity;
    private int select_id;

    public MyPagerAdapter(HomeActivity homeActivity,Bundle bundle) {
        super(homeActivity);
        this.total = bundle.getString("total");
        this.idle = bundle.getString("idle");
        this.toll1 = bundle.getString("toll1");
        this.toll2 = bundle.getString("toll2");
        this.select_id = bundle.getInt("select_id");
        this.homeActivity = homeActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 根据位置返回不同的 Fragment
        switch (position){
            case 0:
                return new InformationFragment();
            case 1:
                return new ControlFragment();
            case 2:
                return new PayFragment();
            case 3:
                return new UserFragment();
            default:
                return new InformationFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;  // 假设有 4 个页面
    }
}