package com.zcw.fingerprintdemo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.zcw.base.CommonUtils;
import com.zcw.fingerprintdemo.App;
import com.zcw.fingerprintdemo.FingerFragment;
import com.zcw.fingerprintdemo.Preference;
import com.zcw.fingerprintdemo.R;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.reactivex.annotations.NonNull;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 * 指纹工具类
 */
@SuppressLint("NewApi")
public class FingerUtil extends FingerprintManager.AuthenticationCallback {
    private static final String KEY = "finger_key";

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

    public FingerUtil(@NonNull Context context, @NonNull Callback callback) {
        this.context = context;
        this.callback = callback;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.fingerprintManager = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
        }
    }

    /**
     *
     */
    /**
     * 开始指纹识别
     * @param purpose 指纹识别类型，{@link android.security.keystore.KeyProperties#PURPOSE_ENCRYPT}为加密；<br />
     *                      {@link android.security.keystore.KeyProperties#PURPOSE_DECRYPT}为解密；
     */
    public void startAuthenticate(int purpose) {
        setPurpose(purpose);
        if(fingerprintManager == null) {
            CommonUtils.toast(context, "设备不支持指纹功能");
            return ;
        }

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get an instance of Cipher", e);
        }

        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
        }
        if(getPurpose() == KeyProperties.PURPOSE_ENCRYPT) {
            createKey(keyStore, keyGenerator);
        }

        try {
            SecretKey key = (SecretKey) keyStore.getKey(KEY, null);

            if(getPurpose() == KeyProperties.PURPOSE_ENCRYPT) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
            else {
                String iv = Preference.getString(Preference.FINGER_KEY_IV, App.app.preferences);
                if(key == null || iv.equals("")) {
                    callback.onError(ERROR_CLOSE, context.getString(R.string.finger_authenticate_failed));
                    return ;
                }

                byte[] ivByte = Base64.decode(iv, Base64.URL_SAFE);
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivByte));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    /**
     * 生成密钥
     * @param keyStore 密钥库
     * @param keyGenerator 生成密钥工具类
     */
    private SecretKey createKey(KeyStore keyStore, KeyGenerator keyGenerator) {
        try {
            keyStore.load(null);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEY,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            keyGenerator.init(builder.build());
            return keyGenerator.generateKey();     // 生成密钥
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        FingerprintManager.CryptoObject cryptoObject = result.getCryptoObject();
        if(cryptoObject == null) {
            callback.onError(ERROR_CLOSE, context.getString(R.string.finger_authenticate_failed));
            return ;
        }

        Cipher cipher = cryptoObject.getCipher();
        try {
            String data;
            if(getPurpose() == KeyProperties.PURPOSE_ENCRYPT) {
                // 保存加密后的数据
                byte[] encryptData = cipher.doFinal(FingerFragment.SECRET_MESSAGE.getBytes());
                data = Base64.encodeToString(encryptData, Base64.URL_SAFE);
                String keyIV = Base64.encodeToString(cipher.getIV(), Base64.URL_SAFE);
                Preference.putString(Preference.FINGER_DATA, data, App.app.preferences);
                Preference.putString(Preference.FINGER_KEY_IV, keyIV, App.app.preferences);
            }
            else {
                // 解密数据
                String encrypt = Preference.getString(Preference.FINGER_DATA, App.app.preferences);
                byte[] encryptByte = cipher.doFinal(Base64.decode(encrypt, Base64.URL_SAFE));
                data = new String(encryptByte);
            }
            callback.onAuthenticated(data);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(ERROR_CLOSE, context.getString(R.string.finger_authenticate_failed));
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
