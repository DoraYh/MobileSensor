package com.qianzuncheng.sensors;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import java.util.concurrent.Semaphore;

public class MyApplication extends Application {
    // TODO: save queue after finish APP
    // public volatile static Queue<Cache.FilePack> uploadFileQueue;
    // public static Lock uploadFileQueueLock;
    // both video capture & audio recording need to record audio
    // and audio recording may run background
    // since acquiring audio & video resources run in main thread
    // reentrant lock won't work
    // public static Lock audioDeviceLock;
    // OK, FINE, use semaphore will block the main thread if acquire() runs in main thread
    // so start a new thread anyway, both reentrant lock & semaphore are OK
    public static Semaphore audioDeviceSemaphore;

    public static String lastTakenPhotoPath;
    public static String lastShotVideoPath;
    public static int textDataCollectCount = 0;
    //public static long last

    // to check network connection
    public static String MAGIC_CONNECTION_KEY = "SYSU_SMC";

    public static Handler mainActivityHandler;

    private static MyApplication instance;

    public MyApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // uploadFileQueue = new LinkedList<>();
        // uploadFileQueueLock = new ReentrantLock();
        // audioDeviceLock = new ReentrantLock();
        audioDeviceSemaphore = new Semaphore(1);
    }
}
