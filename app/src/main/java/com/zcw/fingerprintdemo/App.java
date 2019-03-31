package com.zcw.fingerprintdemo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.zcw.base.LogUtil;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 */
public class App extends Application {

    public static App app;

    public SharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        preferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        // 初始化Log工具
        LogUtil.syncIsDebug(this);
    }
}
