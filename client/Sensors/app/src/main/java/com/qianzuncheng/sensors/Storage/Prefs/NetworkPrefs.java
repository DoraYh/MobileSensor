package com.qianzuncheng.sensors.Storage.Prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.qianzuncheng.sensors.Storage.Config;

public class NetworkPrefs {
    public static final String PREFS_NAME = "NetworkPrefs";
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private String serverAddress;
    public static final String serverAddressDefault = "http://192.168.199.227:80";
    private String serverAddressKey     = "serverAddress";

    public NetworkPrefs(Context context) {
        settings = context.getSharedPreferences(PREFS_NAME, 0);
        editor = settings.edit();

        serverAddress = settings.getString(serverAddressKey, serverAddressDefault);
    }

    public void setDefault() {
        serverAddress = serverAddressDefault;
        editor.putString(serverAddressKey, serverAddressDefault);
        editor.commit();
    }

    public String getServerAddress() {
        return serverAddress;
    }
    public String setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
        editor.putString(serverAddressKey, serverAddress);
        editor.commit();
        return Config.SUCCESS_FLAG;
    }
}
