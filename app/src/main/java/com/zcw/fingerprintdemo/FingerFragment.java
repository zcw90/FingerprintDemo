package com.zcw.fingerprintdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zcw.base.CommonUtils;
import com.zcw.fingerprintdemo.util.FingerUtil;

/**
 * Created by 朱城委 on 2019/3/28.<br><br>
 */
public class FingerFragment extends DialogFragment {

    private FingerUtil fingerUtil;

    /** 显示提示图标 */
    private ImageView imgIcon;

    /** 用来显示提示信息 */
    private TextView tvHint;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
    }

    private void init() {
        fingerUtil = new FingerUtil(getActivity());
        fingerUtil.setCallback(new FingerUtil.Callback() {
            @Override
            public void onAuthenticated() {
                CommonUtils.toast(getActivity(), "指纹识别成功");
                dismiss();
            }

            @Override
            public void onError(int code, String message) {
                showError(message);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finger_dialog, container, false);
        initView(view);

        return view;
    }

    private void initView(View view) {
        imgIcon = view.findViewById(R.id.img_icon_finger);
        tvHint = view.findViewById(R.id.tv_finger_recognition_hint);
        view.findViewById(R.id.btn_finger_recognition_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fingerUtil.stopAuthenticate();
                dismiss();
            }
        });

        fingerUtil.startAuthenticate();
    }

    @Override
    public void onPause() {
        super.onPause();
        fingerUtil.stopAuthenticate();
    }

    private void showError(String message) {
        imgIcon.setImageResource(R.drawable.ic_fingerprint_error);
        tvHint.setText(message);
        tvHint.setTextColor(tvHint.getResources().getColor(R.color.warning_color));
    }
}
