package com.qianzuncheng.sensors;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.qianzuncheng.sensors.Network.UploadJobScheduleService;
import com.qianzuncheng.sensors.Network.UploadQueue;
import com.qianzuncheng.sensors.Network.UploadService;
import com.qianzuncheng.sensors.Storage.Cache;
import com.qianzuncheng.sensors.Storage.Config;
import com.qianzuncheng.sensors.UI.AppInfoDialogFragment;
import com.qianzuncheng.sensors.Utils.AudioUtils;
import com.qianzuncheng.sensors.Utils.DataCollectUtils;
import com.qianzuncheng.sensors.Utils.PhotoUtils;
import com.qianzuncheng.sensors.Utils.VideoUtils;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    public static final String TAG = "MainActivity";

    private TextView cacheView1;
    private TextView cacheView2;
    private TextView cacheView3;
    private TextView networkView;
    private TextView cacheSizeView1;
    private TextView cacheSizeView2;
    private TextView cacheSizeView3;
    private TextView cacheSizeView4;
    //private TextView audioRecordView;
    //private ImageButton audioRecordButton;
    // private ImageButton takeShotButton;
    //private ImageButton captureVideoButton;
    private ConstraintLayout topBar;
    private Chronometer audioChronometer;
    private BottomNavigationView bottomNavigationView;

    private volatile boolean isUserRecordingAudio = false;
    private Intent audioRecordIntent;

    private int networkLogPointer; // for display purpose

    // those not assigned are dynamic
    // declared as "public static" to reach from outside this class
    // keep them "readonly" from outside by eye checking
    public static boolean dataCollectBackground;
    public static float dataCollectServicePeriod; // in seconds
    public static int accelerometerSampleCount;
    public static float accelerometerSampleTime;
    public static float cacheViewUpdatePeriod = 5.05f;
    public static float networkViewUpdatePeriod = 1;
    public static float audioRecordPeriod;
    public static float audioRecordTime;
    //public static float audioRecordViewUpdatePeriod = 0.5f;
    public static boolean audioRecordBackground;
    public static boolean appRunBackground;
    public static String serverAddress;
    public static boolean uploadBackground;
    public static float uploadPeriod;
    public static int maxUploadCount;
    public static int uploadStrategyCode;

    private Timer dataCollectServiceTimer;
    private Timer cacheViewUpdateTimer;
    private Timer networkUpdateTimer;
    private Timer networkViewUpdateTimer;
    private Timer audioRecordTimer;
    //private Timer audioRecordViewUpdateTimer;

    // start activity for result
    public static final int TAKE_PHOTO_CODE = 0;
    public static final int TAKE_VIDEO_CODE = 1;
    public static final int SETTINGS_CODE   = 2;

    // handler
    public static final int HANDLER_AUDIO_RECORD_START  = 0;
    public static final int HANDLER_AUDIO_RECORD_STOP   = 1;

    // JobService
    public static final int UPLOAD_JOB_ID = 0;

    // Ask for permissions
    public static final int PERMISSION_CAMERA_PHOTO_INDEX                   = 0;
    public static final int PERMISSION_CAMERA_VIDEO_INDEX                   = 1;
    public static final int PERMISSION_READ_EXTERNAL_STORAGE_PHOTO_INDEX    = 2;
    public static final int PERMISSION_READ_EXTERNAL_STORAGE_VIDEO_INDEX    = 3;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE_PHOTO_INDEX   = 4;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE_VIDEO_INDEX   = 5;
    public static final int PERMISSION_RECORD_AUDIO_AUTO_INDEX              = 6;
    public static final int PERMISSION_RECORD_AUDIO_MANU_INDEX              = 7;
    public static final int PERMISSION_RECORD_AUDIO_VIDEO_INDEX             = 8;
    public static final int PERMISSION_ACCESS_COARSE_LOCATION_INDEX         = 9;
    public static final int PERMISSION_ACCESS_FINE_LOCATION_INDEX           = 10;
    public static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] PERMISSIONS_EXPLAINATION = {
            "Need camera to take photos",
            "Need camera to shoot videos",
            "Need to read photo from external storage",
            "Need to read video from external storage",
            "Need to save photo to external storage",
            "Need to save video to external storage",
            "Need permission to record audio",
            "Need permission to record audio",
            "Need permission to record audio",
            "Need to collect location data",
            "Need to collect location data"
    };

    private Alarm alarm = new Alarm();

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initHandler();
        updateConfigFromStorage();

        audioRecordIntent = new Intent(this, AudioUtils.class);
        networkLogPointer = -1;

        // stop the alarm if App has been started previously
        alarm.cancelAlarm(this);
        // stop JobScheduler
        if (Build.VERSION.SDK_INT >= 21) {
            cancleUploadJob();
        }

        startTimers();
    }

    private void initView() {
        cacheView1                  = findViewById(R.id.cacheView1);
        cacheView2                  = findViewById(R.id.cacheView2);
        cacheView3                  = findViewById(R.id.cacheView3);
        networkView                 = findViewById(R.id.networkView);
        cacheSizeView1              = findViewById(R.id.cacheSizeView1);
        cacheSizeView2              = findViewById(R.id.cacheSizeView2);
        cacheSizeView3              = findViewById(R.id.cacheSizeView3);
        cacheSizeView4              = findViewById(R.id.cacheSizeView4);
        //audioRecordButton           = findViewById(R.id.audioRecordButton);
        //audioRecordView             = findViewById(R.id.audioRecordView);
        //takeShotButton              = findViewById(R.id.takeShotButton);
        //captureVideoButton          = findViewById(R.id.captureVideoButton);
        topBar                      = findViewById(R.id.topBar);
        audioChronometer            = findViewById(R.id.audioChronometer);
        bottomNavigationView        = findViewById(R.id.bottomNavigationView);

        // magic trick
        bottomNavigationView.getMenu().getItem(0).setCheckable(false);
        bottomNavigationView.getMenu().getItem(1).setCheckable(false);
        bottomNavigationView.getMenu().getItem(2).setCheckable(false);

        // TODO: Android 6.0 Init Permission, need permission from user setting
        /*
        audioRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AudioRecordDialogFragment fragment = AudioRecordDialogFragment.newInstance(MainActivity.this);
                fragment.show(getFragmentManager(), AudioRecordDialogFragment.class.getSimpleName());
                fragment.setOnCancelListener(new AudioRecordDialogFragment.OnAudioCancelListener() {
                    @Override
                    public void onCancel() {
                        fragment.stopRecord();
                        fragment.dismiss();
                    }
                });


                // press stop button
                if(isUserRecordingAudio) {
                    // stop user recording and resume timer
                    stopService(audioRecordIntent);
                    resumeAudioRecordTimer();
                    // UI
                    audioRecordButton.setImageDrawable(
                            getResources().getDrawable(R.drawable.ic_audio_record)
                    );
                    audioChronometer.stop();
                    audioChronometer.setVisibility(View.GONE);
                    // flag
                    isUserRecordingAudio = false;
                } else { // press start button
                    // stop background recording & timer and start user recording
                    stopAudioRecordTimer();
                    stopService(audioRecordIntent);
                    startService(audioRecordIntent);
                    // UI
                    audioRecordButton.setImageDrawable(
                            getResources().getDrawable(R.drawable.ic_audio_stop)
                    );
                    audioChronometer.setVisibility(View.VISIBLE);
                    audioChronometer.setBase(SystemClock.elapsedRealtime());
                    audioChronometer.start();
                    // flag
                    isUserRecordingAudio = true;
                }
            }
        });

        takeShotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PhotoUtils.takeOneShot(MainActivity.this);
            }
        });


        captureVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoUtils.captureVideo(MainActivity.this);
            }
        });
        */

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.photoButton:
                if(needToCheckPermissions()) {
                    if(!checkAndRequestPermissions(PERMISSION_CAMERA_PHOTO_INDEX)
                            || !checkAndRequestPermissions(PERMISSION_READ_EXTERNAL_STORAGE_PHOTO_INDEX)
                            || !checkAndRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE_PHOTO_INDEX)) {
                        return false;
                    }
                }
                //item.setCheckable(true); // magic trick
                PhotoUtils.takeOneShot(MainActivity.this);
                //item.setCheckable(false);
                return true;
            case R.id.audioButton:
                item.setCheckable(false); // refresh status
                item.setCheckable(true);
                if(isUserRecordingAudio) { // press stop button
                    // stop user recording and resume timer
                    stopService(audioRecordIntent);
                    resumeAudioRecordTimer();
                    // UI
                    item.setIcon(getResources().getDrawable(R.drawable.ic_audio_record));
                    item.setTitle(getString(R.string.main_button_record));
                    item.setCheckable(false);
                    // flag
                    isUserRecordingAudio = false;
                } else { // press start button
                    // check permission
                    if(needToCheckPermissions()) {
                        // granted
                        if(!checkAndRequestPermissions(PERMISSION_RECORD_AUDIO_MANU_INDEX)) {
                            return false;
                        }
                    }
                    // stop background recording & timer and start user recording
                    stopAudioRecordTimer();
                    stopService(audioRecordIntent);
                    startService(audioRecordIntent);
                    // UI
                    item.setIcon(getResources().getDrawable(R.drawable.ic_audio_stop));
                    item.setTitle(R.string.main_button_record1);
                    item.setCheckable(true);
                    // flag
                    isUserRecordingAudio = true;
                }
                return true;
            case R.id.videoButton:
                // check permissions
                if(needToCheckPermissions()) {
                    if(!checkAndRequestPermissions(PERMISSION_RECORD_AUDIO_VIDEO_INDEX)
                            || !checkAndRequestPermissions(PERMISSION_CAMERA_VIDEO_INDEX)
                            || !checkAndRequestPermissions(PERMISSION_READ_EXTERNAL_STORAGE_VIDEO_INDEX)
                            || !checkAndRequestPermissions(PERMISSION_WRITE_EXTERNAL_STORAGE_VIDEO_INDEX)) {
                        return false;
                    }
                }
                //item.setCheckable(true);
                VideoUtils.captureVideo(MainActivity.this);
                //item.setCheckable(false);
        }
        return false;
    }

    private void initHandler() {
        MyApplication.mainActivityHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLER_AUDIO_RECORD_START:
                        topBar.setVisibility(View.VISIBLE);
                        audioChronometer.setBase(SystemClock.elapsedRealtime());
                        audioChronometer.start();
                        break;
                    case HANDLER_AUDIO_RECORD_STOP:
                        audioChronometer.stop();
                        topBar.setVisibility(View.GONE);
                        break;
                }
            }
        };
    }

    private void showWarning(String warning) {
        Toast.makeText(this,
                warning,
                Toast.LENGTH_SHORT
        ).show();
    }
    private void showWarning(String warning, boolean ifLong) {
        Toast.makeText(this,
                warning,
                ifLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT
        ).show();
    }

    private void updateConfigFromStorage() {
        appRunBackground            = Config.getInstance(this).getAppPrefs().isRunBackground();
        dataCollectBackground       = Config.getInstance(this).getDataCollectPrefs().isRunService();
        dataCollectServicePeriod    = Config.getInstance(this).getDataCollectPrefs().getCollectPeriod();
        accelerometerSampleCount    = Config.getInstance(this).getDataCollectPrefs().getAccelerometerSampleCount();
        accelerometerSampleTime     = Config.getInstance(this).getDataCollectPrefs().getAccelerometerSampleTime();
        audioRecordTime             = Config.getInstance(this).getAudioPrefs().getAudioLength();
        audioRecordPeriod           = Config.getInstance(this).getAudioPrefs().getAudioPeriod();
        audioRecordBackground       = Config.getInstance(this).getAudioPrefs().isRunBackground();
        serverAddress               = Config.getInstance(this).getNetworkPrefs().getServerAddress();
        uploadBackground            = Config.getInstance(this).getUploadPrefs().isRunBackground();
        uploadPeriod                = Config.getInstance(this).getUploadPrefs().getUploadPeriod();
        maxUploadCount              = Config.getInstance(this).getUploadPrefs().getMaxUploadCount();
        uploadStrategyCode          = Config.getInstance(this).getUploadPrefs().getStrategyCode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_CODE);
                return true;
            case R.id.info:
                final AppInfoDialogFragment fragment = AppInfoDialogFragment.newInstance(MainActivity.this);
                fragment.show(getFragmentManager(), AppInfoDialogFragment.class.getSimpleName());
                fragment.setOnCancelListener(new AppInfoDialogFragment.OnAppInfoCancelListener() {
                    @Override
                    public void onCancel() {
                        fragment.dismiss();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void resumeAudioRecordTimer() {
        if(audioRecordTimer == null) {
            audioRecordTimer = new Timer();
        }
        setAudioRecordTimerTask();
    }

    public void stopAudioRecordTimer() {
        if(audioRecordTimer != null) {
            audioRecordTimer.cancel();
            audioRecordTimer = null;
        }
    }

    private void startTimers() {
        if(dataCollectServiceTimer == null) {
            dataCollectServiceTimer = new Timer();
        }
        setDataCollectServiceTimerTask();

        if(cacheViewUpdateTimer == null) {
            cacheViewUpdateTimer = new Timer();
        }
        setCacheViewUpdateTimerTask();
        if(networkViewUpdateTimer == null) {
            networkViewUpdateTimer = new Timer();
        }
        setNetworkViewUpdateTimerTask();


        // TODO: use Alarm, currently using Timer for demo
        if(networkUpdateTimer == null) {
            networkUpdateTimer = new Timer();
        }
        setNetworkUpdateTimerTask();


        if(audioRecordTimer == null) {
            audioRecordTimer = new Timer();
        }
        setAudioRecordTimerTask();

        /*
        if(audioRecordViewUpdateTimer == null) {
            audioRecordViewUpdateTimer = new Timer();
        }
        setAudioRecordViewUpdateTimerTask();
        */
    }

    private void stopTimers() {
        if(dataCollectServiceTimer != null) {
            dataCollectServiceTimer.cancel();
            dataCollectServiceTimer = null;
        }
        if(cacheViewUpdateTimer != null) {
            cacheViewUpdateTimer.cancel();
            cacheViewUpdateTimer = null;
        }
        if(networkUpdateTimer != null) {
            networkUpdateTimer.cancel();
            networkUpdateTimer = null;
        }
        if(networkViewUpdateTimer != null) {
            networkViewUpdateTimer.cancel();
            networkViewUpdateTimer = null;
        }
        if(audioRecordTimer != null) {
            audioRecordTimer.cancel();
            audioRecordTimer = null;
        }

        /*
        if(audioRecordViewUpdateTimer != null) {
            audioRecordViewUpdateTimer.cancel();
            audioRecordViewUpdateTimer = null;
        }
        */
    }

    // to apply new config
    private void restartTimers() {
        stopTimers();
        startTimers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume MainActivity");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: pause MainActivity");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        UploadQueue.getInstance(this).save();
        if(appRunBackground) {
            alarm.setAlarm(this);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 21) {
                scheduleUploadJob();
            } else {
                // unfortunately...
            }
            alarm.cancelAlarm(this);
        }
        Log.d(TAG, "onDestroy: destroy MainActivity");
        stopTimers();
        super.onDestroy();
    }

    void setDataCollectServiceTimerTask() {
        if(!dataCollectBackground) return;
        if(needToCheckPermissions()) {
            if(!checkAndRequestPermissions(PERMISSION_ACCESS_COARSE_LOCATION_INDEX)
                    || !checkAndRequestPermissions(PERMISSION_ACCESS_FINE_LOCATION_INDEX)) {
                return;
            }
        }
        dataCollectServiceTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, DataCollectUtils.class);
                startService(intent);
            }
        }, 0, (long) (dataCollectServicePeriod * 1000));
    }

    void setCacheViewUpdateTimerTask() {
        int delay = 3 * 1000;
        cacheViewUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String cacheData = Cache.getInstance(MainActivity.this).read(Cache.CATEGORY.DATA_COLLECT_SERVICE);
                if(cacheData == null) return;
                String[] splittedCacheData = cacheData.split("\n");
                DataCollectUtils.Data data = new DataCollectUtils.Data();
                data.parse(splittedCacheData[splittedCacheData.length - 1]);

                final SpannableString[] cacheSizeStringArray = data.toSpannableStringArray();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cacheView1.setText(cacheSizeStringArray[0], TextView.BufferType.SPANNABLE);
                        cacheView2.setText(cacheSizeStringArray[1], TextView.BufferType.SPANNABLE);
                        cacheView3.setText(cacheSizeStringArray[2], TextView.BufferType.SPANNABLE);
                    }
                });
            }
        }, delay, (long) (cacheViewUpdatePeriod * 1000));
    }

    private void setNetworkUpdateTimerTask() {
        if(!uploadBackground) return;
        networkUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isMyServiceRunning(UploadService.class)) {
                    Intent intent = new Intent(MainActivity.this, UploadService.class);
                    startService(intent);
                }
            }
        }, 0, (long) (uploadPeriod * 1000));
    }

    private void setNetworkViewUpdateTimerTask() {
        networkViewUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Update network condition
                String networkLog = Cache.getInstance(MainActivity.this).read(Cache.CATEGORY.NETWORK_LOG);
                if(networkLog == null) return;
                String[] splittedNetworkLog = networkLog.split("\n");
                if(networkLogPointer == -1 || networkLogPointer >= splittedNetworkLog.length) {
                    networkLogPointer = splittedNetworkLog.length - 1;
                }
                com.qianzuncheng.sensors.Network.Log.Entry logEntry = new com.qianzuncheng.sensors.Network.Log.Entry(
                        splittedNetworkLog[networkLogPointer]);
                networkLogPointer++;

                // Update cache size view
                Cache.Size.updateCacheSize(MainActivity.this);

                final SpannableString networkLogString = logEntry.toSpannableString();
                final SpannableString cacheSizeString[] = Cache.Size.toSpannableStringArray();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        networkView.setText(networkLogString, TextView.BufferType.SPANNABLE);
                        cacheSizeView1.setText(cacheSizeString[0], TextView.BufferType.SPANNABLE);
                        cacheSizeView2.setText(cacheSizeString[1], TextView.BufferType.SPANNABLE);
                        cacheSizeView3.setText(cacheSizeString[2], TextView.BufferType.SPANNABLE);
                        cacheSizeView4.setText(cacheSizeString[3], TextView.BufferType.SPANNABLE);
                    }
                });
            }
        }, 0, (long) (networkViewUpdatePeriod * 1000));
    }

    private void setAudioRecordTimerTask() {
        if(!audioRecordBackground) return;
        // check permission
        if(needToCheckPermissions()) {
            if(!checkAndRequestPermissions(PERMISSION_RECORD_AUDIO_AUTO_INDEX)) {
                return;
            }
        }
        // that "audioRecordTime > audioRecordPeriod" is checked on the setting activity
        audioRecordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                startService(audioRecordIntent);
                try {
                    Thread.sleep((long) (audioRecordTime * 1000));
                } catch (Exception e) {}
                if(!isUserRecordingAudio) {
                    // if user is recording, then don't stop
                    stopService(audioRecordIntent);
                }
            }
        }, 10 * 1000, (long) (audioRecordPeriod * 1000));
    }

    /*
    private void setAudioRecordViewUpdateTimerTask() {
        audioRecordViewUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(isMyServiceRunning(AudioUtils.class)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //audioRecordView.setText("Recording...");
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //audioRecordView.setText("Idle...");
                        }
                    });
                }
            }
        }, 0, (long) (audioRecordViewUpdatePeriod * 1000));
    }
    */

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    // take one shot result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            //Log.d("CameraLog", "Pic saved");
            // String filePath = MyApplication.lastTakenPhotoPath;
            /*
            Cache.getInstance(this).append(
                    Cache.CATEGORY.UPLOAD_QUEUE,
                    new Cache.FilePack(filePath, Cache.CATEGORY.PHOTO)

            );*/
            UploadQueue.getInstance(this).add(
                    new Cache.FilePack(MyApplication.lastTakenPhotoPath, Cache.CATEGORY.PHOTO)
            );
        }
        else if (requestCode == TAKE_VIDEO_CODE) {
            // don't need RESULT_OK to release semaphore
            MyApplication.audioDeviceSemaphore.release();
            if(resultCode == RESULT_OK) {
                UploadQueue.getInstance(this).add(
                        new Cache.FilePack(MyApplication.lastShotVideoPath, Cache.CATEGORY.VIDEO)
                );
            }
        }
        else if (requestCode == SETTINGS_CODE && resultCode == RESULT_OK) {
            updateConfigFromStorage();
            restartTimers();
        }
    }

    // do upload when conditions are met after the app is killed
    @TargetApi(21)
    public void scheduleUploadJob() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(
                UPLOAD_JOB_ID, new ComponentName(this, UploadJobScheduleService.class)
        );
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // wifi
        builder.setRequiresCharging(true);
        builder.setPeriodic(15 * 1000); // 15 min
        //builder.setRequiresDeviceIdle(true);
        jobScheduler.schedule(builder.build());
    }

    @TargetApi(21)
    private void cancleUploadJob() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(UPLOAD_JOB_ID);
    }



    /*
    *
    *                            Permissions
    *        Android 6.0 or higher needs to check permissions dynamically
    *
     */
    private boolean needToCheckPermissions() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }


    /*
    *   Permissions include
    *   Manifest.permission.CAMERA
    *   Manifest.permission.READ_EXTERNAL_STORAGE
    *   Manifest.permission.WRITE_EXTERNAL_STORAGE
    *   Manifest.permission.RECORD_AUDIO
    *   Manifest.permission.ACCESS_COARSE_LOCATION
    *   Manifest.permission.ACCESS_FINE_LOCATION
    *
     */
    private boolean checkAndRequestPermissions(final int permissionIndex) {
        String permission = PERMISSIONS[permissionIndex];
        int permissionStatus = ContextCompat.checkSelfPermission(this, permission);
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            /*
            // user denied but tried again
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showDialogOK(PERMISSIONS_EXPLAINATION[permissionIndex],
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        checkAndRequestPermissions(permissionIndex);
                                        break;
                                    //case DialogInterface.BUTTON_NEGATIVE:
                                        // proceed with logic by disabling the related features or quit the app.
                                    //    break;
                                }
                            }
                        });
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{permission},
                        permissionIndex);
            }
            return false;
            */
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{permission},
                    permissionIndex);
            return false;
        }
        // permission granted
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @Nullable String permissions[], @Nullable int[] grantResults) {
        final int permissionIndex = requestCode; // yes, they're equal, see array <PERMISSIONS> please
        // permission was granted
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (permissionIndex) {
                case PERMISSION_CAMERA_PHOTO_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(0)); // at pos 0
                    break;
                case PERMISSION_CAMERA_VIDEO_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(2)); // at pos 2
                    break;
                case PERMISSION_READ_EXTERNAL_STORAGE_PHOTO_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(0)); // at pos 0
                    break;
                case PERMISSION_READ_EXTERNAL_STORAGE_VIDEO_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(2)); // at pos 2
                    break;
                case PERMISSION_WRITE_EXTERNAL_STORAGE_PHOTO_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(0)); // at pos 2
                    break;
                case PERMISSION_WRITE_EXTERNAL_STORAGE_VIDEO_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(2)); // at pos 2
                    break;
                case PERMISSION_RECORD_AUDIO_AUTO_INDEX:
                    setAudioRecordTimerTask();
                    break;
                case PERMISSION_RECORD_AUDIO_MANU_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(1)); // at pos 1
                    // UIs
                    bottomNavigationView.getMenu().getItem(1).setChecked(true);
                    break;
                case PERMISSION_RECORD_AUDIO_VIDEO_INDEX:
                    onNavigationItemSelected(bottomNavigationView.getMenu().getItem(2)); // at pos 2
                    break;
                case PERMISSION_ACCESS_COARSE_LOCATION_INDEX:
                    setDataCollectServiceTimerTask();
                    break;
                case PERMISSION_ACCESS_FINE_LOCATION_INDEX:
                    setDataCollectServiceTimerTask();
                    break;
            }
        } else {

        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                //.setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}
