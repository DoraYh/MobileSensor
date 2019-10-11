package com.qianzuncheng.sensors.Network;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qianzuncheng.sensors.MainActivity;
import com.qianzuncheng.sensors.Storage.Cache;

import java.io.File;


// TODO: check IO service
public class UploadService extends IntentService {
    public static final String TAG = "UploadService";
    // see https://developer.android.com/studio/run/emulator-networking
    // public static final String DEBUG_SERVER_URL = "http://10.0.2.2:80/";

    public static class STATUS {
        public static final String NETWORK_DISCONNECT   = "Network Disconnect";
        public static final String IDLE                 = "Idle";
        public static final String UPLOADING            = "Uploading";
        public static final String UPLOAD_FAILED        = "Upload Failed";
        public static final String UPLOAD_SUCCESS       = "Upload Success";
    }

    public static class STRATEGY {
        public static final String FCFS = "FirstComeFirstServed";
        public static final String SFF  = "SmallFilesFirst";
        public static final String FB   = "FitBandwidth";

        public static final int FCFS_CODE = 0;
        public static final int SFF_CODE  = 1;
        public static final int FB_CODE   = 2;
    }

    public UploadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Lifecycle: onHandleIntent: upload start");
        // updateQueue();

        /*
        // test helper
        if(intent != null && intent.getBooleanExtra("test", false)) {
            MyApplication.uploadFileQueueLock.lock();
            if(!MyApplication.uploadFileQueue.isEmpty()) {
                Cache.FilePack filePack = MyApplication.uploadFileQueue.poll();
                // duplicate upload
                upload(filePack);
                upload(filePack);
            }
            MyApplication.uploadFileQueueLock.unlock();
            return;
        }
        */

        int count = 0;
        int MAX_UPLOAD_COUNT = MainActivity.maxUploadCount;
        UploadQueue.getInstance(this).setStrategy(MainActivity.uploadStrategyCode);

        // while uploading, guarantee NO modifying queue from other thread
        /* MyApplication.uploadFileQueueLock.lock();
        while(!MyApplication.uploadFileQueue.isEmpty() && count < MAX_UPLOAD_COUNT) {
            Cache.FilePack filePack = MyApplication.uploadFileQueue.poll();
            //File file = new File(filePack.filePath);
            //if(!file.exists()) {
            //    continue;
            //}
            upload(filePack);
            count++;
        }
        MyApplication.uploadFileQueueLock.unlock();
        */
        while(count < MAX_UPLOAD_COUNT) {
            Cache.FilePack filePack = UploadQueue.getInstance(this).getNext();
            if(filePack == null) break;
            asyncUpload(filePack);
            count++;
        }

    }

    /*
    private void updateQueue() {
        // no need to update queue while it's NOT empty
        if(!MyApplication.uploadFileQueue.isEmpty()) return;

        MyApplication.uploadFileQueueLock.lock();
        try {
            // find all local files for upload
            for (String category : Cache.CATEGORY.allUploadCategory()) {
                String[] filePaths = Cache.getInstance(this).getFilePaths(category);
                for (String fp : filePaths) {
                    File file = new File(fp);
                    if(file.length() > 0) {
                        MyApplication.uploadFileQueue.offer(new Cache.FilePack(fp, category));
                    } else {
                        // for those files whose size is 0, normally this shouldn't happen
                        Cache.getInstance(this).clear(file);
                    }
                }
            }

            /*
            // write this file in different services / activities
            String queueString = Cache.getInstance(this).read(Cache.CATEGORY.UPLOAD_QUEUE);
            String[] pieces = queueString.split("\n");
            for (String piece: pieces) {
                if(piece == "") continue;
                String[] filePack = piece.split("\t");
                MyApplication.uploadFileQueue.offer(new Cache.FilePack(filePack[0], filePack[1]));
            }
            Cache.getInstance(this).clear(Cache.CATEGORY.UPLOAD_QUEUE);
        } catch (Exception e) {
            android.util.Log.e(TAG, "updateQueue: " + e.toString());
        } finally {
            MyApplication.uploadFileQueueLock.unlock();
        }
    }
    */

    private boolean asyncUpload(Cache.FilePack filePack) {
        File file = new File(filePack.filePath);
        //if(!isFileWritable(file)) return false;
        // TODO: check writable
        String detail = filePack.category + ";" + file.getName();
        HttpPostTask httpPostTask = new HttpPostTask();
        httpPostTask.doPostFileAsync(MainActivity.serverAddress, filePack, detail);
        /*
        HttpPostTask httpPostTask = new HttpPostTask();
        httpPostTask.execute(SERVER_URL, file.getAbsolutePath(), detail);
        */

        return true;
    }

    // below is a stupid function
    /*
    private boolean isFileWritable(File f) {
        try {
            FileWriter fw = new FileWriter(f.getAbsolutePath());
            fw.close();
        } catch (IOException e) {
            android.util.Log.i(TAG, "isFileWritable: " + e.toString());
            return false;
        }
        return true;
    }
    */
}
