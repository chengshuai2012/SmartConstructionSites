package com.aoecloud.smartconstructionsites.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtils {
    private static SharedPreferences pictureSpUtils;

    private static SharedPreferences getSp(Context context) {
        if (pictureSpUtils == null) {
            pictureSpUtils = context.getSharedPreferences("my_sp", Context.MODE_PRIVATE);
        }
        return pictureSpUtils;
    }

    public static String getString(Context context, String key) {
        return getSp(context).getString(key,"");
    }
    public static void putString(Context context, String key, String value) {
        getSp(context).edit().putString(key, value).apply();
    }

    public static void putBoolean(Context context, String key, boolean value) {
        getSp(context).edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return getSp(context).getBoolean(key, defValue);
    }
}
