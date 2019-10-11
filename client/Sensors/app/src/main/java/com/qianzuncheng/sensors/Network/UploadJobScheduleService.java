package com.qianzuncheng.sensors.Network;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;

import com.qianzuncheng.sensors.MainActivity;
import com.qianzuncheng.sensors.Storage.Cache;

import java.io.File;

@TargetApi(21)
public class UploadJobScheduleService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        int count = 0;
        int MAX_UPLOAD_COUNT = MainActivity.maxUploadCount;
        UploadQueue.getInstance(this).setStrategy(MainActivity.uploadStrategyCode);
        while(count < MAX_UPLOAD_COUNT) {
            final Cache.FilePack filePack = UploadQueue.getInstance(this).getNext();
            if(filePack == null) break;
            // run in main thread, only in background
            // so block UI thread is OK
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    syncUpload(filePack); // sync
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {}
            count++;
        }
        return false; // means this task has been done
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private boolean syncUpload(Cache.FilePack filePack) {
        File file = new File(filePack.filePath);
        String detail = filePack.category + ";" + file.getName();
        HttpPostTask httpPostTask = new HttpPostTask();
        httpPostTask.doPostFileSync(MainActivity.serverAddress, filePack, detail);
        return true;
    }
}
