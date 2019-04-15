package com.zcw.fingerprintdemo.util;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by 朱城委 on 2019/4/1.<br><br>
 * 加密相关类相关类
 */
public class FingerSecurity {
    /** 密钥库别名 */
    private static final String KEYSTORE_ALIAS = "fingerKeystore";

    /** 密钥库 */
    private KeyStore keyStore;

    /** 密钥生成工具 */
    private KeyGenerator keyGenerator;

    /** 用于加密、解密 */
    private Cipher cipher;

    public FingerSecurity() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        } catch (Exception e) {
            throw new RuntimeException("Failed to get an instance of FingerSecurity.", e);
        }
    }

    /**
     * 生成密钥
     * @return
     */
    private SecretKey generateKey() {
        try {
            // 指明密钥的目的，这里指定为加密、解密
            int purpose = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(KEYSTORE_ALIAS, purpose)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

                keyGenerator.init(builder.build());
                return keyGenerator.generateKey();     // 生成密钥
            }
            else {
                throw new RuntimeException("android.os.Build.VERSION.SDK_INT < 23(Android 6.0)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Generate key failed.", e);
        }
    }

    /**
     * 初始化{@link Cipher}
     * @param purpose 用于指明是加密，还是解密。<br />
     *                加密传入{@link KeyProperties#PURPOSE_ENCRYPT};<br />
     *                解密传入{@link KeyProperties#PURPOSE_DECRYPT}。
     * @param iv 初始向量，解密才需要传入，如果是加密，传入null即可。
     * @return 如果初始化失败，返回null。
     */
    public Cipher initCipher(int purpose, String iv) {
        if(purpose != KeyProperties.PURPOSE_ENCRYPT && purpose != KeyProperties.PURPOSE_DECRYPT) {
            throw new IllegalArgumentException("Unknown purpose: " + purpose);
        }

        try {
            if (purpose == KeyProperties.PURPOSE_ENCRYPT) {
                SecretKey key = generateKey();
                cipher.init(purpose, key);
            } else {
                SecretKey key = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
                if (key == null || iv == null || iv.equals("")) {
                    return null;
                }

                cipher.init(purpose, key, new IvParameterSpec(Base64.decode(iv, Base64.URL_SAFE)));
            }
            return cipher;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
