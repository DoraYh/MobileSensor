package com.qianzuncheng.sensors.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.qianzuncheng.sensors.MainActivity;
import com.qianzuncheng.sensors.MyApplication;

import java.io.File;
import java.io.IOException;

public class VideoUtils {
    public static final String TAG = "VideoUtils";
    public static final String dirName = "Sensors";

    private static final String FILE_PROVIDER_AUTHORITY = "com.qianzuncheng.sensors.fileprovider";
    private static final String filenamePrefix = "video";

    public static void captureVideo(final Activity activity) {
        // check dir
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + dirName + "/";
        File newdir = new File(dir);
        if(!newdir.exists()) {
            newdir.mkdirs();
        }


        // create file
        long startingTimeMillis = System.currentTimeMillis();
        String filePath = String.format("%s%s_%s.mp4",
                dir, filenamePrefix, startingTimeMillis);
        File newFile = new File(filePath);
        try {
            newFile.createNewFile();
        } catch (IOException e) {}


        // pull up camera activity
        final Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {// prevent app crash when no camera detected
            Uri outputFileUri = Uri.fromFile(newFile);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                outputFileUri = FileProvider.getUriForFile(activity, FILE_PROVIDER_AUTHORITY, newFile);
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            //MyApplication.lastTakenPhotoPath = filePath;
            MyApplication.lastShotVideoPath = filePath;

            // try to start activity in new thread
            // so that acquiring semaphore won't block UI thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // acquire semaphore
                        if(!MyApplication.audioDeviceSemaphore.tryAcquire()) { // for display purpose
                            // show toast
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(
                                            activity,
                                            "Waiting for audio recoding to stop...",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });
                            // block this thread to acquire semaphore
                            MyApplication.audioDeviceSemaphore.acquire();
                        }


                        // start new activity to capture video
                        activity.startActivityForResult(cameraIntent, MainActivity.TAKE_VIDEO_CODE);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "captureVideo: " + e.toString());
                    }
                }
            }).start();
        }
    }
}


/*
public class VideoUtils extends Activity implements View.OnClickListener, SurfaceHolder.Callback {
    public static final String dirName = "Sensors";
    private static final String filenamePrefix = "video";

    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private boolean recording = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        recorder = new MediaRecorder();
        initRecorder();
        setContentView(R.layout.video_capture);

        SurfaceView cameraView = findViewById(R.id.CameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        cameraView.setClickable(true);
        cameraView.setOnClickListener(this);
    }

    private void initRecorder() {
        MyApplication.audioDeviceLock.lock();

        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/" + dirName + "/";
        File newdir = new File(dir);
        if(!newdir.exists()) {
            newdir.mkdirs();
        }
        long startingTimeMillis = System.currentTimeMillis();
        String filePath = String.format("%s%s_%s.mp4",
                dir, filenamePrefix, startingTimeMillis);

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recorder.setOutputFile(filePath);
        recorder.setMaxDuration(60 * 1000); // 60 seconds
        //recorder.setMaxFileSize(5000000); // Approximately 5 megabytes
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());

        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            recording = false;

            // Let's initRecorder so we can record again
            initRecorder();
            prepareRecorder();
        } else {
            recording = true;
            recorder.start();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        prepareRecorder();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        MyApplication.audioDeviceLock.unlock();
        finish();
    }
}
*/