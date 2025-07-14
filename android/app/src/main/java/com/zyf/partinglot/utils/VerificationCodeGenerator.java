package com.zyf.partinglot.utils;

import java.util.Random;

public class VerificationCodeGenerator {

    /**
     * 生成一个6位数字的验证码.
     *
     * @return 返回一个6位数字的字符串形式验证码
     */
    public static String generateVerificationCode() {
        // 定义随机数对象
        Random random = new Random();
        // 生成一个从100000到999999之间的随机整数（即6位数）
        int code = 100000 + random.nextInt(900000);
        // 将整数转换为字符串并返回
        return String.valueOf(code);
    }

}