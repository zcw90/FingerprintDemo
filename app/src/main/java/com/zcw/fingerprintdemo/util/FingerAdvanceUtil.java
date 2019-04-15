package com.zcw.fingerprintdemo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.zcw.base.CommonUtils;
import com.zcw.fingerprintdemo.App;
import com.zcw.fingerprintdemo.FingerFragment;
import com.zcw.fingerprintdemo.Preference;
import com.zcw.fingerprintdemo.R;

import javax.crypto.Cipher;

import io.reactivex.annotations.NonNull;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 * 指纹进阶工具类
 */
@SuppressLint("NewApi")
public class FingerAdvanceUtil extends FingerUtil {
    private static final String TAG = FingerAdvanceUtil.class.getSimpleName();

    /** 用于表示指纹识别是加密，还是解密 */
    private int purpose;

    /** 加密相关工具类 */
    private FingerSecurity fingerSecurity;

    public FingerAdvanceUtil(@NonNull Context context, @NonNull Callback callback) {
        super(context, callback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerSecurity = new FingerSecurity();
        }
    }

    /**
     * 开始指纹识别
     * @param purpose 指纹识别类型。<br />
     *                  {@link KeyProperties#PURPOSE_ENCRYPT}为加密；<br />
     *                  {@link KeyProperties#PURPOSE_DECRYPT}为解密；
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

    public int getPurpose() {
        return purpose;
    }

    public void setPurpose(int purpose) {
        this.purpose = purpose;
    }

    /**
     * 指纹识别成功，会关闭指纹传感器。
     * @param result
     */
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);

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
}
