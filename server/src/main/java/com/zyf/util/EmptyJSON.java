package com.zyf.util;

import org.json.JSONObject;

/**
 * JSON消息工具类
 * 用于生成符合协议规范的JSON消息
 */
public class EmptyJSON {

    // 协议版本
    private static final String PROTOCOL_VERSION = "1.0";
    // 消息来源
    private static final String SOURCE = "server";
    // 消息目标
    private static final String DESTINATION = "android";

    /**
     * 生成data类型的JSON消息
     * @return JSONObject 包含基本协议字段的JSON对象
     */
    public static JSONObject getForData() {
        JSONObject root = new JSONObject();
        root.put("protocolVersion", PROTOCOL_VERSION);
        root.put("messageType", "data");
        root.put("timestamp", System.currentTimeMillis() / 1000);
        root.put("source", SOURCE); 
        root.put("destination", DESTINATION);
        return root;
    }

    /**
     * 生成command类型的JSON消息
     * @return JSONObject 包含基本协议字段的JSON对象
     */
    public static JSONObject getForCommand() {
        JSONObject root = new JSONObject();
        root.put("protocolVersion", PROTOCOL_VERSION);
        root.put("messageType", "command");
        root.put("timestamp", System.currentTimeMillis() / 1000);
        root.put("source", SOURCE);
        root.put("destination", DESTINATION);
        return root;
    }

}
