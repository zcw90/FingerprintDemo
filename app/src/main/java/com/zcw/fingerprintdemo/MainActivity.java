package com.zcw.fingerprintdemo;

import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.zcw.fingerprintdemo.util.Util;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvOriginData;
    private TextView tvEncryptData;
    private TextView tvDecryptData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        findViewById(R.id.btn_start_finger_recognition_encrypt).setOnClickListener(this);
        findViewById(R.id.btn_start_finger_recognition_decrypt).setOnClickListener(this);
        tvOriginData = findViewById(R.id.tv_origin_data);
        tvEncryptData = findViewById(R.id.tv_encrypt_data);
        tvDecryptData = findViewById(R.id.tv_decrypt_data);

        tvOriginData.setText(tvOriginData.getText().toString() + FingerFragment.SECRET_MESSAGE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_finger_recognition_encrypt:
                startFingerRecognition(KeyProperties.PURPOSE_ENCRYPT);
                break;

            case R.id.btn_start_finger_recognition_decrypt:
                startFingerRecognition(KeyProperties.PURPOSE_DECRYPT);
                break;
        }
    }

    /**
     * 开始指纹识别
     * @param purpose 指纹识别类型，{@link android.security.keystore.KeyProperties#PURPOSE_ENCRYPT}为加密；<br />
     *                      {@link android.security.keystore.KeyProperties#PURPOSE_DECRYPT}为解密；
     */
    private void startFingerRecognition(int purpose) {
        if(!Util.isFingerAvailable(MainActivity.this)) {
            return ;
        }

        FingerFragment fragment = new FingerFragment();
        fragment.setPurpose(purpose);
        fragment.show(getSupportFragmentManager(), "FingerFragment");
    }

    /**
     * 显示加密后的数据
     * @param message
     */
    public void setEncryptData(String message) {
        String data = "密文：" + message;
        tvEncryptData.setText(data);
    }

    /**
     * 显示解密后的数据
     * @param message
     */
    public void setDecryptData(String message) {
        String data = "明文：" + message;
        tvDecryptData.setText(data);
    }
}
