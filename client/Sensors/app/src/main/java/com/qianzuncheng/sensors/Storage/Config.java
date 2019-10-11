package com.qianzuncheng.sensors.Storage;

import android.content.Context;

import com.qianzuncheng.sensors.Storage.Prefs.AppPrefs;
import com.qianzuncheng.sensors.Storage.Prefs.AudioPrefs;
import com.qianzuncheng.sensors.Storage.Prefs.DataCollectPrefs;
import com.qianzuncheng.sensors.Storage.Prefs.NetworkPrefs;
import com.qianzuncheng.sensors.Storage.Prefs.UploadPrefs;

public class Config {
    public static final String TAG = "Config";
    private volatile static Config uniqueInstance;
    private Context context;

    public static final String SUCCESS_FLAG = "SUCCESS";

    private AppPrefs appPrefs;
    private AudioPrefs audioPrefs;
    private DataCollectPrefs dataCollectPrefs;
    private NetworkPrefs networkPrefs;
    private UploadPrefs uploadPrefs;

    private Config(Context context) {
        this.context = context;
        audioPrefs          = new AudioPrefs(context);
        appPrefs            = new AppPrefs(context);
        dataCollectPrefs    = new DataCollectPrefs(context);
        networkPrefs        = new NetworkPrefs(context);
        uploadPrefs         = new UploadPrefs(context);
    }
    // Double CheckLock(DCL)
    public static Config getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (Config.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new Config(context);
                }
            }
        }
        return uniqueInstance;
    }

    public AppPrefs getAppPrefs() { return appPrefs; }
    public AudioPrefs getAudioPrefs() {
        return audioPrefs;
    }
    public DataCollectPrefs getDataCollectPrefs() {
        return dataCollectPrefs;
    }
    public NetworkPrefs getNetworkPrefs() {
        return networkPrefs;
    }
    public UploadPrefs getUploadPrefs() {
        return uploadPrefs;
    }
}
