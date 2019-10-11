package com.qianzuncheng.sensors.Utils;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;

import com.qianzuncheng.sensors.MainActivity;
import com.qianzuncheng.sensors.MyApplication;
import com.qianzuncheng.sensors.Network.UploadQueue;
import com.qianzuncheng.sensors.Storage.Cache;

import java.io.File;
import java.io.IOException;

public class AudioUtils extends Service {
    public static final String TAG = "AudioUtils";

    private String filepathPrefix;
    private static final String filenamePrefix = "audio";
    private String filePath;

    private MediaRecorder recorder;

    private boolean semaphoreAcquired;

    public AudioUtils() {
        semaphoreAcquired = false;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        filepathPrefix = Cache.getInstance(this).getFileDirpath(Cache.CATEGORY.AUDIO);
        startRecording();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (recorder != null) {
            stopRecording();
        }
        super.onDestroy();
    }

    public void startRecording() {
        Log.d(TAG, "Lifecycle: startRecording: start! ");
        if(!MyApplication.audioDeviceSemaphore.tryAcquire()) {
            // capturing video
            Log.d(TAG, "startRecording: " + "failed to obtain semaphore!");
            return;
        }
        semaphoreAcquired = true;
        Log.d(TAG, "startRecording: " + "obtained semaphore!");
        long startingTimeMillis = System.currentTimeMillis();
        File folder = new File(filepathPrefix);
        if (!folder.exists()) {
            folder.mkdir();
        }
        filePath = String.format("%s%s_%s.mp4",
                filepathPrefix, filenamePrefix, startingTimeMillis);

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // no encoding tools for MP3
        recorder.setOutputFile(filePath);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        recorder.setAudioSamplingRate(44100);
        recorder.setAudioEncodingBitRate(192000);

        try {
            recorder.prepare();
            recorder.start();
            Log.d(TAG, "startRecording: start recording");
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        // UI
        MyApplication.mainActivityHandler.sendEmptyMessage(MainActivity.HANDLER_AUDIO_RECORD_START);
    }

    public void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            UploadQueue.getInstance(this).add(
                    new Cache.FilePack(filePath, Cache.CATEGORY.AUDIO)
            );
        } catch (RuntimeException e) {
            // may double stop
        } finally {
            if (semaphoreAcquired) {
                MyApplication.audioDeviceSemaphore.release();
                Log.d(TAG, "stopRecording: release semaphore");
            }
            // UI
            MyApplication.mainActivityHandler.sendEmptyMessage(MainActivity.HANDLER_AUDIO_RECORD_STOP);
        }
        /*
        Cache.getInstance(this).append(
                Cache.CATEGORY.UPLOAD_QUEUE,
                new Cache.FilePack(filePath, Cache.CATEGORY.AUDIO)
        );
        */
    }
}
