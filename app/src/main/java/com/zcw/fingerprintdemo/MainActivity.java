package com.zcw.fingerprintdemo;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zcw.base.CommonUtils;
import com.zcw.fingerprintdemo.util.Util;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        findViewById(R.id.btn_start_finger_recognition).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_finger_recognition:
                startFingerRecognition();
                break;
        }
    }

    /**
     * 开始指纹识别
     */
    private void startFingerRecognition() {
        if(!Util.isFingerAvailable(MainActivity.this)) {
            return ;
        }

        FingerFragment fragment = new FingerFragment();
        fragment.show(getSupportFragmentManager(), "FingerFragment");
    }
}
