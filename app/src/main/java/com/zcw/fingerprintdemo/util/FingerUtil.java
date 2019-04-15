package com.zcw.fingerprintdemo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

import io.reactivex.annotations.NonNull;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 * 指纹工具类
 */
@SuppressLint("NewApi")
public abstract class FingerUtil extends FingerprintManager.AuthenticationCallback {
    private static final String TAG = FingerUtil.class.getSimpleName();

    /** 指纹识别错误码，会关闭指纹传感器 */
    public static final int ERROR_CLOSE = 101;

    /** 指纹识别错误码，不会关闭指纹传感器 */
    public static final int ERROR_NOT_CLOSE = 102;

    protected Context context;
    protected FingerprintManager fingerprintManager;

    /** 用于取消指纹识别 */
    protected CancellationSignal cancellationSignal;

    protected Callback callback;

    public FingerUtil(@NonNull Context context, @NonNull Callback callback) {
        this.context = context;
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        }
    }

    /**
    * 开始指纹识别
    * @param purpose 指纹识别类型。<br />
    *                  {@link android.security.keystore.KeyProperties#PURPOSE_ENCRYPT}为加密；<br />
    *                  {@link android.security.keystore.KeyProperties#PURPOSE_DECRYPT}为解密；
    */
    public abstract void startAuthenticate(int purpose);

    /**
     * 关闭指纹识别
     */
    public void stopAuthenticate() {
        if(cancellationSignal != null) {
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    /**
     * 发生不可恢复的错误，会关闭指纹传感器。
     * @param errorCode
     * @param errString
     */
    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        callback.onError(ERROR_CLOSE, errString.toString());
    }

    /**
     * 发生可恢复的错误，不会关闭指纹传感器，比如手指移动太快。
     * @param helpCode
     * @param helpString
     */
    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        callback.onError(helpCode, helpString.toString());
    }

    /**
     * 指纹识别失败，不会关闭指纹传感器，可再次识别。
     */
    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        callback.onError(ERROR_NOT_CLOSE, "识别失败，再试一次");
    }

    /**
     * 指纹识别回调
     */
    public interface Callback {
        /**
         * 识别成功回调
         * @param message 加密后的数据
         */
        void onAuthenticated(String message);

        /**
         * 识别失败回调
         * @param code 错误代码
         * @param message 错误信息
         */
        void onError(int code, String message);
    }
}
