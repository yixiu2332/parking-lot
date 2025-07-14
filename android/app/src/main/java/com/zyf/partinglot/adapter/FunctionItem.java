package com.zyf.partinglot.adapter;

public class FunctionItem {
    private final int iconResId;
    private final String title;

    public FunctionItem(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }
}
