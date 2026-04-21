package com.zcm.hotwordinsight.preprocess.step.impl;

public class StrUtil {

    public static boolean isBlank(String data) {
        return data == null || data.trim().isEmpty();
    }

    public static boolean isNotBlank(String data) {
        return !isBlank(data);
    }

}
