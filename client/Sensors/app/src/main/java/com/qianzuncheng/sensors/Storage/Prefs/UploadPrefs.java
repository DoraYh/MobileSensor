package com.qianzuncheng.sensors.Storage.Prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.qianzuncheng.sensors.Storage.Config;

public class UploadPrefs {
    public static final String PREFS_NAME = "UploadPrefs";
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private boolean runBackground;
    private float uploadPeriod;
    private int maxUploadCount;
    private int strategyCode;
    public static final boolean runBackgroundDefault = true;
    public static final float uploadPeriodDefault = 10f;
    public static final int maxUploadCountDefault = 5;
    public static final int strategyCodeDefault = 0;
    private String runBackgroundKey     = "runBackground";
    private String uploadPeriodKey      = "uploadPeriod";
    private String maxUploadCountKey    = "maxUploadCount";
    private String strategyCodeKey      = "strategyCode";

    public UploadPrefs(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        runBackground   = settings.getBoolean(runBackgroundKey, runBackgroundDefault);
        uploadPeriod    = settings.getFloat(uploadPeriodKey, uploadPeriodDefault);
        maxUploadCount  = settings.getInt(maxUploadCountKey, maxUploadCountDefault);
        strategyCode    = settings.getInt(strategyCodeKey, strategyCodeDefault);
    }

    public void setDefault() {
        runBackground   = runBackgroundDefault;
        uploadPeriod    = uploadPeriodDefault;
        maxUploadCount  = maxUploadCountDefault;
        strategyCode    = strategyCodeDefault;
        editor.putBoolean(runBackgroundKey, runBackgroundDefault);
        editor.putFloat(uploadPeriodKey, uploadPeriodDefault);
        editor.putInt(maxUploadCountKey, maxUploadCountDefault);
        editor.putInt(strategyCodeKey, strategyCodeDefault);
        editor.commit();
    }

    public boolean isRunBackground() {
        return runBackground;
    }
    public String setRunbackground(boolean runBackground) {
        this.runBackground = runBackground;
        editor.putBoolean(runBackgroundKey, runBackground);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public float getUploadPeriod() {
        return uploadPeriod;
    }
    public String setUploadPeriod(float uploadPeriod) {
        if(uploadPeriod < 1e-3) return "Auto Upload Period must be greater than 0 !";
        this.uploadPeriod = uploadPeriod;
        editor.putFloat(uploadPeriodKey, uploadPeriod);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public int getMaxUploadCount() {
        return maxUploadCount;
    }
    public String setMaxUploadCount(int maxUploadCount) {
        if(maxUploadCount < 0) return "Max Number of Upload Files mustn't be less than 0 !";
        this.maxUploadCount = maxUploadCount;
        editor.putInt(maxUploadCountKey, maxUploadCount);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public int getStrategyCode() {
        return strategyCode;
    }
    public String setStrategyCode(int strategyCode) {
        this.strategyCode = strategyCode;
        editor.putInt(strategyCodeKey, strategyCode);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }
}
