package com.zyf.partinglot.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class EmptyJSON {


    public static JSONObject getForData() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("protocolVersion", "1.0");
        root.put("messageType", "data");
        root.put("timestamp", System.currentTimeMillis() / 1000);
        root.put("source", "android");
        root.put("destination", "server");
        return root;
    }

    public static JSONObject getForCommand() throws JSONException {
        JSONObject root = new JSONObject();
        root.put("protocolVersion", "1.0");
        root.put("messageType", "command");
        root.put("timestamp", System.currentTimeMillis() / 1000);
        root.put("source", "android");
        root.put("destination", "server");
        return root;
    }

}
