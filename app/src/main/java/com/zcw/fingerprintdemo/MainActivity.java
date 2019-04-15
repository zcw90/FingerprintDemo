package com.zcw.fingerprintdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TYPE_FINGER = "type_finger";
    public static final int TYPE_FINGER_SIMPLE = 10;
    public static final int TYPE_FINGER_ADVANCE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        findViewById(R.id.btn_finger_simple).setOnClickListener(this);
        findViewById(R.id.btn_finger_advance).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_finger_simple:
                startActivity(SimpleActivity.class, MainActivity.TYPE_FINGER_SIMPLE);
                break;

            case R.id.btn_finger_advance:
                startActivity(AdvanceActivity.class, MainActivity.TYPE_FINGER_ADVANCE);
                break;
        }
    }

    private void startActivity(Class<?> cls, int type) {
        Intent intent = new Intent(MainActivity.this, cls);
        intent.putExtra(TYPE_FINGER, type);
        startActivity(intent);
    }
}
