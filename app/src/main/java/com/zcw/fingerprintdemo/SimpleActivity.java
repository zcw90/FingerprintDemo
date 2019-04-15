package com.zcw.fingerprintdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zcw.fingerprintdemo.util.Util;

/**
 * Created by 朱城委 on 2019/4/15.<br><br>
 * 指纹的简单使用
 */
public class SimpleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        init();
    }

    private void init() {
        findViewById(R.id.btn_simple_finger_recognition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFingerRecognition();
            }
        });
    }

    /**
     * 开始指纹识别
     */
    private void startFingerRecognition() {
        if(!Util.isFingerAvailable(SimpleActivity.this)) {
            return ;
        }

        FingerFragment fragment = new FingerFragment();
        fragment.show(getSupportFragmentManager(), "FingerFragment");
    }
}
