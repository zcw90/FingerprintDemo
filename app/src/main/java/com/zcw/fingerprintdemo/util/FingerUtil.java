package com.zcw.fingerprintdemo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.zcw.base.CommonUtils;
import com.zcw.base.LogUtil;
import com.zcw.fingerprintdemo.App;
import com.zcw.fingerprintdemo.FingerFragment;
import com.zcw.fingerprintdemo.Preference;
import com.zcw.fingerprintdemo.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.crypto.Cipher;

import io.reactivex.annotations.NonNull;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 * 指纹工具类
 */
@SuppressLint("NewApi")
public class FingerUtil extends FingerprintManager.AuthenticationCallback {
    private static final String TAG = FingerUtil.class.getSimpleName();

    /** 指纹识别错误码，会关闭指纹传感器 */
    public static final int ERROR_CLOSE = 101;

    /** 指纹识别错误码，不会关闭指纹传感器 */
    public static final int ERROR_NOT_CLOSE = 102;

    private Context context;
    private FingerprintManager fingerprintManager;

    /** 用于取消指纹识别 */
    private CancellationSignal cancellationSignal;

    private Callback callback;

    /** 用于表示指纹识别是加密，还是解密 */
    private int purpose;

    /** 加密相关工具类 */
    private FingerSecurity fingerSecurity;

    public FingerUtil(@NonNull Context context, @NonNull Callback callback) {
        this.context = context;
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
            fingerSecurity = new FingerSecurity();
        }
    }

    /**
     * 开始指纹识别
     * @param purpose 指纹识别类型。<br />
     *                  {@link android.security.keystore.KeyProperties#PURPOSE_ENCRYPT}为加密；<br />
     *                  {@link android.security.keystore.KeyProperties#PURPOSE_DECRYPT}为解密；
     */
    public void startAuthenticate(int purpose) {
        if(purpose != KeyProperties.PURPOSE_ENCRYPT && purpose != KeyProperties.PURPOSE_DECRYPT) {
            throw new IllegalArgumentException("Unknown purpose: " + purpose);
        }

        if(fingerprintManager == null) {
            CommonUtils.toast(context, "设备不支持指纹功能");
            return ;
        }

        setPurpose(purpose);
        String iv = purpose == KeyProperties.PURPOSE_ENCRYPT ? null : Preference.getString(Preference.FINGER_KEY_IV, App.app.preferences);
        Cipher cipher = fingerSecurity.initCipher(purpose, iv);
        if(cipher == null) {
            callback.onError(ERROR_CLOSE, context.getString(R.string.finger_authenticate_failed));
            return ;
        }

        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
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

    public int getPurpose() {
        return purpose;
    }

    public void setPurpose(int purpose) {
        this.purpose = purpose;
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
     * 指纹识别成功，不会关闭指纹传感器。
     * @param result
     */
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

        try {
//            Field field = result.getClass().getDeclaredField("mFingerprint");
            Field field = result.getClass().getField("mFingerprint");
            field.setAccessible(true);
            Object fingerPrint = field.get(result);

            Class<?> cls = Class.forName("android.hardware.fingerprint.Fingerprint");
            Method getName = cls.getDeclaredMethod("getName");
            Method getFingerId = cls.getDeclaredMethod("getFingerId");
            Method getGroupId = cls.getDeclaredMethod("getGroupId");
            Method getDeviceId = cls.getDeclaredMethod("getDeviceId");

            CharSequence name = (CharSequence) getName.invoke(fingerPrint);
            int fingerId = (int) getFingerId.invoke(fingerPrint);
            int groupId = (int) getGroupId.invoke(fingerPrint);
            long deviceId = (long) getDeviceId.invoke(fingerPrint);

            LogUtil.e(TAG, "Name: " + name);
            LogUtil.e(TAG, "FingerId: " + fingerId);
            LogUtil.e(TAG, "GroupId: " + groupId);
            LogUtil.e(TAG, "DeviceId: " + deviceId);
        }
        catch (Exception e) {
            e.printStackTrace();
            callback.onError(ERROR_CLOSE, context.getString(R.string.finger_authenticate_failed));
            return ;
        }


        FingerprintManager.CryptoObject cryptoObject = result.getCryptoObject();
        if(cryptoObject == null) {
            callback.onError(ERROR_CLOSE, context.getString(R.string.finger_authenticate_failed));
            return ;
        }

        Cipher cipher = cryptoObject.getCipher();
        String data;
        if(getPurpose() == KeyProperties.PURPOSE_ENCRYPT) {
            try {
                // 保存加密后的数据
                byte[] encryptData = cipher.doFinal(FingerFragment.SECRET_MESSAGE.getBytes());
                data = Base64.encodeToString(encryptData, Base64.URL_SAFE);
                String keyIV = Base64.encodeToString(cipher.getIV(), Base64.URL_SAFE);
                Preference.putString(Preference.FINGER_DATA, data, App.app.preferences);
                Preference.putString(Preference.FINGER_KEY_IV, keyIV, App.app.preferences);
                callback.onAuthenticated(data);
            }
            catch (Exception e) {
                e.printStackTrace();
                callback.onError(ERROR_CLOSE, context.getString(R.string.finger_authenticate_failed));
            }
        }
        else {
            try {
                // 解密数据
                String encrypt = Preference.getString(Preference.FINGER_DATA, App.app.preferences);
                byte[] encryptByte = cipher.doFinal(Base64.decode(encrypt, Base64.URL_SAFE));
                data = new String(encryptByte);
                callback.onAuthenticated(data);
            }
            catch (Exception e) {
                e.printStackTrace();
                callback.onError(ERROR_CLOSE, context.getString(R.string.finger_change));
            }
        }
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
