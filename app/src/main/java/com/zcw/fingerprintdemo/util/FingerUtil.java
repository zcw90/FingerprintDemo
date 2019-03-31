package com.zcw.fingerprintdemo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

import com.zcw.base.CommonUtils;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 * 指纹工具类
 */
@SuppressLint("NewApi")
public class FingerUtil extends FingerprintManager.AuthenticationCallback {
    /** 指纹识别错误码，会关闭指纹传感器 */
    public static final int ERROR_CLOSE = 101;

    /** 指纹识别错误码，不会关闭指纹传感器 */
    public static final int ERROR_NOT_CLOSE = 102;

    private Context context;
    private FingerprintManager fingerprintManager;

    /** 用于取消指纹识别 */
    private CancellationSignal cancellationSignal;

    private Callback callback;

    public FingerUtil(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        }
    }

    /**
     * 开始指纹识别
     */
    public void startAuthenticate() {
        if(fingerprintManager == null) {
            CommonUtils.toast(context, "设备不支持指纹功能");
            return ;
        }

        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(null, cancellationSignal, 0, this, null);
    }

    /**
     * 关闭指纹识别
     */
    public void stopAuthenticate() {
        if(cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * 发生不可恢复的错误，会关闭指纹传感器。
     * @param errorCode
     * @param errString
     */
    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if(callback != null) {
            callback.onError(ERROR_CLOSE, errString.toString());
        }
    }

    /**
     * 发生可恢复的错误，不会关闭指纹传感器，比如手指移动太快。
     * @param helpCode
     * @param helpString
     */
    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        if(callback != null) {
            callback.onError(helpCode, helpString.toString());
        }
    }

    /**
     * 指纹识别成功，不会关闭指纹传感器。
     * @param result
     */
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if(callback != null) {
            callback.onAuthenticated();
        }
    }

    /**
     * 指纹识别失败，不会关闭指纹传感器，可再次识别。
     */
    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if(callback != null) {
            callback.onError(ERROR_NOT_CLOSE, "识别失败，再试一次");
        }
    }

    /**
     * 指纹识别回调
     */
    public interface Callback {
        /**
         * 识别成功回调
         */
        void onAuthenticated();

        /**
         * 识别失败回调
         * @param code 错误代码
         * @param message 错误信息
         */
        void onError(int code, String message);
    }
}
