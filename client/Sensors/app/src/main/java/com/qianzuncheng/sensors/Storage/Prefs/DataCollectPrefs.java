package com.qianzuncheng.sensors.Storage.Prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.qianzuncheng.sensors.Storage.Config;

public class DataCollectPrefs {
    public static final String PREFS_NAME = "DataCollectPrefs";
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private boolean runService;
    private float collectPeriod;
    private int accelerometerSampleCount;
    private float accelerometerSampleTime;
    public static final boolean runServiceDefault = true;
    public static final float collectPeriodDefault = 5;
    public static final int accelerometerSampleCountDefault = 10;
    public static final float accelerometerSampleTimeDefault = 2;
    private String runServiceKey                = "runService";
    private String collectPeriodKey             = "collectPeriod";
    private String accelerometerSampleCountKey  = "accelerometerSampleCount";
    private String accelerometerSampleTimeKey   = "accelerometerSampleTime";


    public DataCollectPrefs(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        runService                  = settings.getBoolean(runServiceKey, runServiceDefault);
        collectPeriod               = settings.getFloat(collectPeriodKey, collectPeriodDefault);
        accelerometerSampleCount    = settings.getInt(accelerometerSampleCountKey, accelerometerSampleCountDefault);
        accelerometerSampleTime     = settings.getFloat(accelerometerSampleTimeKey, accelerometerSampleTimeDefault);
    }

    public void setDefault() {
        runService                  = runServiceDefault;
        collectPeriod               = collectPeriodDefault;
        accelerometerSampleCount    = accelerometerSampleCountDefault;
        accelerometerSampleTime     = accelerometerSampleTimeDefault;
        editor.putBoolean(runServiceKey, runServiceDefault);
        editor.putFloat(collectPeriodKey, collectPeriodDefault);
        editor.putInt(accelerometerSampleCountKey, accelerometerSampleCountDefault);
        editor.putFloat(accelerometerSampleTimeKey, accelerometerSampleTimeDefault);
        editor.commit();
    }

    public boolean isRunService() {
        return runService;
    }
    public String setRunService(boolean runService) {
        this.runService = runService;
        editor.putBoolean(runServiceKey, runService);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public float getCollectPeriod() {
        return collectPeriod;
    }
    public String setCollectPeriod(float collectPeriod) {
        if(collectPeriod < 1e-3) return "Collect text data period must be greater than 0 !";
        this.collectPeriod = collectPeriod;
        editor.putFloat(collectPeriodKey, collectPeriod);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public int getAccelerometerSampleCount() {
        return accelerometerSampleCount;
    }
    public String setAccelerometerSampleCount(int accelerometerSampleCount) {
        if(accelerometerSampleCount < 1) return "Accelerometer sample count must be greater than 0 !";
        this.accelerometerSampleCount = accelerometerSampleCount;
        editor.putInt(accelerometerSampleCountKey, accelerometerSampleCount);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public float getAccelerometerSampleTime() {
        return accelerometerSampleTime;
    }

    public String setAccelerometerSampleTime(float accelerometerSampleTime) {
        if(collectPeriod < 1e-3) return "Collect accelerometer data period must be greater than 0 !";
        this.accelerometerSampleTime = accelerometerSampleTime;
        editor.putFloat(accelerometerSampleTimeKey, accelerometerSampleTime);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }
}