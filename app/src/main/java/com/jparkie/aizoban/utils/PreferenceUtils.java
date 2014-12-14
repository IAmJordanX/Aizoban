package com.jparkie.aizoban.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jparkie.aizoban.AizobanApplication;
import com.jparkie.aizoban.R;

public class PreferenceUtils {
    public static String getSource() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.preference_source_key), context.getString(R.string.preference_source_default_value));
    }

    public static boolean isLazyLoading() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_lazy_loading_key), true);
    }

    public static boolean isRightToLeftDirection() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_direction_key), false);
    }

    public static void setDirection(boolean isRightToLeftDirection) {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_direction_key), isRightToLeftDirection);
        editor.commit();
    }

    public static boolean isLockOrientation() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_orientation_key), false);
    }

    public static void setOrientation(boolean isLockOrientation) {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_orientation_key), isLockOrientation);
        editor.commit();
    }

    public static boolean isLockZoom() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_zoom_key), false);
    }

    public static void setZoom(boolean isLockZoom) {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.preference_zoom_key), isLockZoom);
        editor.commit();
    }

    public static boolean isWiFiOnly() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_download_wifi_key), true);
    }

    public static boolean isExternalStorage() {
        Context context = AizobanApplication.getInstance();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.preference_download_directory_key), false);
    }
}
