package com.zcw.fingerprintdemo.util;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;

import com.zcw.base.CommonUtils;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 * 工具类
 */
public class Util {

    /**
     * 使用指纹之前的一系列检查，有：<br />
     * 应用是否添加指纹权限；<br />
     * 设备是否支持指纹功能；<br />
     * 设备是否开启锁屏密码；<br />
     * 设备是否录入指纹；<br />
     * @param context
     * @return 如果任一项检查失败，返回false
     */
    public static boolean isFingerAvailable(Context context) {
        if(!CommonUtils.hasPermission(context, Manifest.permission.USE_FINGERPRINT)) {
            CommonUtils.toast(context, "应用未添加指纹权限");
            return false;
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
            KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);

            if(!fingerprintManager.isHardwareDetected()) {
                CommonUtils.toast(context, "设备不支持指纹功能");
                return false;
            }

            if(!keyguardManager.isKeyguardSecure()) {
                CommonUtils.toast(context, "设备未开启锁屏密码");
                return false;
            }

            if(!fingerprintManager.hasEnrolledFingerprints()) {
                CommonUtils.toast(context, "设备未录入指纹");
                return false;
            }

            return true;
        }
        else {
            CommonUtils.toast(context, "设备不支持指纹功能");
            return false;
        }
    }
}
