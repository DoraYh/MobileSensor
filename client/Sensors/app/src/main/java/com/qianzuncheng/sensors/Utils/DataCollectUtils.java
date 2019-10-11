package com.qianzuncheng.sensors.Utils;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;

import com.qianzuncheng.sensors.MainActivity;
import com.qianzuncheng.sensors.MyApplication;
import com.qianzuncheng.sensors.Network.UploadQueue;
import com.qianzuncheng.sensors.R;
import com.qianzuncheng.sensors.Storage.Cache;

import java.io.File;
import java.util.Date;

public class DataCollectUtils extends IntentService {
    public static final int JOB_ID = 1;

    public static final String TAG = "DataCollectUtils";
    private Data data;

    // TODO: data encoding
    public static class Data {
        private String time;
        private String location;
        private String accelerometer;
        public void setData(String t, String l, String a) {
            this.time           = t;
            this.location       = l;
            this.accelerometer  = a;
        }
        public String serialize() {
            String serializedData = String.format(
              "%s/%s/%s",
              time,
              location,
              accelerometer
            );
            return serializedData;
        }
        public void parse(String serializedData) {
            if(serializedData == null) {
                time            = "-1";
                location        = "-1;-1";
                accelerometer   = "-1;-1";
            }
            try {
                String[] t = serializedData.split("/");
                time = t[0];
                location = t[1];
                accelerometer = t[2];
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "parse: " + e.toString());
            }
        }
        public SpannableString[] toSpannableStringArray() {
            String[] showStr = {
                    MyApplication.getContext().getString(R.string.main_list_time) + ": \n", "", "\n\n",
                    MyApplication.getContext().getString(R.string.main_list_location) + ": \n", "", "\n\n",
                    MyApplication.getContext().getString(R.string.main_list_accelerometer) + ": \n", "", "\n\n"
            };

            showStr[1] = time;
            if(location != null) {
                String[] t = location.split(";");
                if(t.length > 1) {
                    showStr[4] = t[0] + " " + t[1];
                }
            }
            if(accelerometer != null) {
                String[] t = accelerometer.split(";");
                if(t.length > 1) {
                    showStr[7] = String.format("%s\n%s\n...8 more", t[0], t[1]);
                }
            }

            int LINE_NUM = 3;
            SpannableString spannableStringArray[] = new SpannableString[3];
            for (int i = 0; i < LINE_NUM; i++) {
                String finalString = "";
                for(int j = 0; j < 3; j++) {
                    finalString += showStr[i * 3 + j];
                }
                spannableStringArray[i] = new SpannableString(finalString);
                // bold
                spannableStringArray[i].setSpan(
                        new StyleSpan(android.graphics.Typeface.BOLD),
                        finalString.indexOf(showStr[i * 3]),
                        finalString.indexOf(showStr[i * 3]) + showStr[i * 3].length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            return spannableStringArray;
        }
    }

    public DataCollectUtils() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Lifecycle: onHandleIntent: data collect start!");
        startForeground(JOB_ID, new Notification());

        int repeatTimes = intent.getIntExtra("repeatTimes", 1);

        for (int repeatCount = 0; repeatCount < repeatTimes; repeatCount++) {
            String time = (new Date()).toString();

            // collect location data
            LocationUtils.getInstance(this).updateLocation();
            String location = LocationUtils.getInstance(this).getLocationString();

            // collect motion data
            MotionUtils.getInstance(this).registerSensor();
            try {
                for(int i = 0; i < MainActivity.accelerometerSampleCount; i++) {
                    MotionUtils.getInstance(this).recordValues();
                    Thread.sleep((long) (MainActivity.accelerometerSampleTime * 1000.0 / MainActivity.accelerometerSampleCount));
                }
            } catch (Exception e) {}
            String accelerometer = MotionUtils.getInstance(this).getValueString();

            LocationUtils.getInstance(this).removeLocationUpdatesListener();
            MotionUtils.getInstance(this).removeMotionUpdatesListener();
            data = new Data();
            data.setData(time, location, accelerometer);
            Cache.getInstance(this).append(Cache.CATEGORY.DATA_COLLECT_SERVICE, data.serialize());

            // rename text data file and offer to upload queue
            MyApplication.textDataCollectCount++;
            if(MyApplication.textDataCollectCount >= 10) {
                MyApplication.textDataCollectCount = 0;
                File dataCollectFile = new File(
                        Cache.getInstance(this).getFileDirpath(Cache.CATEGORY.DATA_COLLECT_SERVICE)
                                + Cache.CATEGORY.DATA_COLLECT_SERVICE
                );
                File newName = new File(dataCollectFile.getAbsolutePath() + "_" + System.currentTimeMillis());
                boolean x = dataCollectFile.renameTo(newName);
                UploadQueue.getInstance(this).add(
                        new Cache.FilePack(newName.getAbsolutePath(), Cache.CATEGORY.DATA_COLLECT_SERVICE)
                );
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
