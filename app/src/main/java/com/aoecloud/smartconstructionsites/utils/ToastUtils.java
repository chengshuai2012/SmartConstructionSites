package com.aoecloud.smartconstructionsites.utils;

import android.text.TextUtils;
import android.widget.Toast;

import com.aoecloud.smartconstructionsites.base.BaseApplication;


public class ToastUtils {

    public static void showToast(String text) {
        if (isFastDoubleClick() && TextUtils.equals(text, mLastText)) {
            return;
        }

        Toast.makeText(BaseApplication.context, text, Toast.LENGTH_SHORT).show();
        mLastText = text;

    }

    private final static long TIME = 1000;
    private static long lastClickTime;
    private static String mLastText;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}