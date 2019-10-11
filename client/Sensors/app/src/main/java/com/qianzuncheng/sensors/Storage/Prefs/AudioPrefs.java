package com.qianzuncheng.sensors.Storage.Prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.qianzuncheng.sensors.Storage.Config;

public class AudioPrefs {
    public static final String PREFS_NAME = "AudioPrefs";
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private boolean runBackground;
    private float audioLength; // in seconds
    private float recordPeriod; // in seconds, must >= audioLength
    public static final boolean runBackgroundDefault = true;
    public static final float audioLengthDefault = 3f;
    public static final float recordPeriodDefault = 15f;
    private String runBackgroundKey = "runBackground";
    private String audioLengthKey   = "audioLength";
    private String recordPeriodKey  = "recordPeriod";
    // set bitrate is unnecessary

    public AudioPrefs(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        runBackground = settings.getBoolean(runBackgroundKey, runBackgroundDefault);
        audioLength = settings.getFloat(audioLengthKey, audioLengthDefault);
        recordPeriod = settings.getFloat(recordPeriodKey, recordPeriodDefault);
    }

    public void setDefault() {
        runBackground   = runBackgroundDefault;
        audioLength     = audioLengthDefault;
        recordPeriod    = recordPeriodDefault;
        editor.putBoolean(runBackgroundKey, runBackgroundDefault);
        editor.putFloat(audioLengthKey, audioLengthDefault);
        editor.putFloat(recordPeriodKey, recordPeriodDefault);
        editor.commit();
    }

    public boolean isRunBackground() {
        return runBackground;
    }
    public String setRunBackground(boolean runBackground) {
        this.runBackground = runBackground;
        editor.putBoolean(runBackgroundKey, runBackground);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public float getAudioLength() {
        return audioLength;
    }
    public String setAudioLength(float audioLength) {
        if(audioLength < 1e-3) return "Audio Length must be greater than 0!";
        this.audioLength = audioLength;
        editor.putFloat(audioLengthKey, audioLength);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }

    public float getAudioPeriod() {
        return recordPeriod;
    }
    public String setAudioPeriod(float recordPeriod) {
        if(recordPeriod < 1e-3) return "Record Period must be greater than 0!";
        this.recordPeriod = recordPeriod;
        editor.putFloat(recordPeriodKey, recordPeriod);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }
}