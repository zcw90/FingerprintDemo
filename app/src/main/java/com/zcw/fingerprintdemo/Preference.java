package com.zcw.fingerprintdemo;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by 朱城委 on 2019/3/31.<br><br>
 * 用于存放偏好设置
 */
public class Preference {

    /** 指纹相关数据 */
    public static final String FINGER_DATA = "finger_data";

    /** 指纹加密key IV */
    public static final String FINGER_KEY_IV = "finger_key_iv";

    /**
     * 存储String数据
     * @param key 要存储的key
     * @param value 要存储的数据
     * @param preferences 存储的配置文件
     */
    public static void putString(String key, String value, SharedPreferences preferences) {
        Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 获取String数据
     * @param key 要获取数据对于的key
     * @param preferences 存储的配置文件
     * @return 如果没有相应数据，返回""。
     */
    public static String getString(String key, SharedPreferences preferences) {
        return preferences.getString(key, "");
    }
}
