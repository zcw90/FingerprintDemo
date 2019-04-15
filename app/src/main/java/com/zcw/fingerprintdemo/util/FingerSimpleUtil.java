package com.zcw.fingerprintdemo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

import com.zcw.base.CommonUtils;
import com.zcw.fingerprintdemo.R;

import io.reactivex.annotations.NonNull;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 * 指纹简单工具类
 */
@SuppressLint("NewApi")
public class FingerSimpleUtil extends FingerUtil {
    private static final String TAG = FingerSimpleUtil.class.getSimpleName();

    public FingerSimpleUtil(@NonNull Context context, @NonNull Callback callback) {
        super(context, callback);
    }

    /**
     * 开始指纹识别
     * @param purpose 指纹识别类型, 简单使用指纹识别，忽略此参数
     */
    @Override
    public void startAuthenticate(int purpose) {
        if(fingerprintManager == null) {
            CommonUtils.toast(context, "设备不支持指纹功能");
            return ;
        }

        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(null, cancellationSignal, 0, this, null);
    }

    /**
     * 指纹识别成功，不会关闭指纹传感器。
     * @param result
     */
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        callback.onAuthenticated(context.getString(R.string.finger_authenticate_success));
    }
}
