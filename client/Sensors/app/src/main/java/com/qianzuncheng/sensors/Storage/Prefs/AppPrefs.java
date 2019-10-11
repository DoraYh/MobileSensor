package com.qianzuncheng.sensors.Storage.Prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.qianzuncheng.sensors.Storage.Config;

public class AppPrefs {
    public static final String PREFS_NAME = "AppPrefs";
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private boolean appRunBackground;
    public static final boolean appRunBackgroundDefault = true;
    private String appRunBackgroundKey = "AppRunBackground";

    public AppPrefs(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        appRunBackground = settings.getBoolean(appRunBackgroundKey, appRunBackgroundDefault);
    }

    public void setDefault() {
        appRunBackground = appRunBackgroundDefault;
        editor.putBoolean(appRunBackgroundKey, appRunBackgroundDefault);
        editor.commit();
    }

    public boolean isRunBackground() {
        return appRunBackground;
    }
    public String setRunBackground(boolean runBackground) {
        this.appRunBackground = runBackground;
        editor.putBoolean(appRunBackgroundKey, runBackground);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }
}
