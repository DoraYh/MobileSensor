package com.qianzuncheng.sensors;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qianzuncheng.sensors.Storage.Cache;
import com.qianzuncheng.sensors.Storage.Config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

// TODO: sample greater than 0
// TODO: show str

public class SettingsActivity extends AppCompatActivity {
    // App
    private Switch appRunBackgroundSwitch;
    // Audio
    private EditText audioRecordTimeInput;
    private EditText audioRecordPeriodInput;
    private Switch audioSwitch;
    private Button clearAudioButton;
    // DataCollect
    private Switch dataCollectSwitch;
    private EditText dataCollectPeriodInput;
    private EditText accelerometerSampleCountInput;
    private EditText accelerometerSampleTimeInput;
    private Button clearTextDataButton;
    private TextView textView26;
    // Network
    private EditText serverAddressInput;
    private Button testConnectionButton;
    // Upload
    private Switch uploadBackgroundSwitch;
    private EditText uploadPeriodInput;
    private EditText maxUploadCountInput;
    private Button uploadAllFilesButton;
    private Spinner uploadStrategySpinner;
    // Photo
    private Button clearPhotoButton;
    // Video
    private Button clearVideoButton;
    // Apply & Rest
    private Button applyButton;
    private Button resetButton;

    // Check Icon
    private ImageView appCheckView;
    private ImageView audioCheckView;
    private ImageView dataCheckView;
    private ImageView networkCheckView;
    private ImageView uploadCheckView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initView();
        setConfigViewFromStorage();
    }

    private void initView() {
        appRunBackgroundSwitch          = findViewById(R.id.appRunBackgroundSwitch);
        audioRecordTimeInput            = findViewById(R.id.audioRecordTimeInput);
        audioRecordPeriodInput          = findViewById(R.id.audioRecordPeriodInput);
        audioSwitch                     = findViewById(R.id.audioSwitch);
        clearAudioButton                = findViewById(R.id.clearAudioData);
        dataCollectSwitch               = findViewById(R.id.dataCollectSwitch);
        dataCollectPeriodInput          = findViewById(R.id.dataCollectTimeInput);
        accelerometerSampleCountInput   = findViewById(R.id.accelerometerSampleCountInput);
        accelerometerSampleTimeInput    = findViewById(R.id.accelerometerSampleTimeInput);
        clearTextDataButton             = findViewById(R.id.clearTextDataButton);
        textView26                      = findViewById(R.id.textView26);
        serverAddressInput              = findViewById(R.id.serverAddressInput);
        testConnectionButton            = findViewById(R.id.testConnectionButton);
        uploadBackgroundSwitch          = findViewById(R.id.uploadBackgroundSwitch);
        uploadPeriodInput               = findViewById(R.id.uploadPeriodInput);
        maxUploadCountInput             = findViewById(R.id.maxUploadCountInput);
        uploadAllFilesButton            = findViewById(R.id.uploadAllButton);
        applyButton                     = findViewById(R.id.applyButton);
        resetButton                     = findViewById(R.id.resetButton);
        uploadStrategySpinner           = findViewById(R.id.uploadStrategySpinner);
        clearPhotoButton                = findViewById(R.id.clearPhotoDataButton);
        clearVideoButton                = findViewById(R.id.clearVideoDataButton);

        appCheckView                    = findViewById(R.id.appCheckView);
        audioCheckView                  = findViewById(R.id.audioCheckView);
        dataCheckView                   = findViewById(R.id.dataCheckView);
        networkCheckView                = findViewById(R.id.networkCheckView);
        uploadCheckView                 = findViewById(R.id.uploadCheckView);

        appCheckView.setVisibility(View.GONE);
        audioCheckView.setVisibility(View.GONE);
        dataCheckView.setVisibility(View.GONE);
        networkCheckView.setVisibility(View.GONE);
        uploadCheckView.setVisibility(View.GONE);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyAllSettings();
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAllPrefsDefault();
            }
        });
        testConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testConnection();
            }
        });
        audioSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                audioRecordTimeInput.setEnabled(isChecked);
                audioRecordPeriodInput.setEnabled(isChecked);
            }
        });
        clearAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cache.getInstance(SettingsActivity.this).clear(Cache.CATEGORY.AUDIO);
                showWarning(getString(R.string.setting_hint_audio));
            }
        });
        dataCollectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                dataCollectPeriodInput.setEnabled(isChecked);
                accelerometerSampleCountInput.setEnabled(isChecked);
                accelerometerSampleTimeInput.setEnabled(isChecked);
            }
        });
        accelerometerSampleCountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                textView26.setText(editable.toString());
            }
        });
        clearTextDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cache.getInstance(SettingsActivity.this).clear(Cache.CATEGORY.DATA_COLLECT_SERVICE);
                showWarning(getString(R.string.setting_hint_textdata));
            }
        });
        uploadBackgroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                uploadPeriodInput.setEnabled(isChecked);
                maxUploadCountInput.setEnabled(isChecked);
            }
        });
        clearPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cache.getInstance(SettingsActivity.this).clear(Cache.CATEGORY.PHOTO);
                showWarning(getString(R.string.setting_hint_photo));
            }
        });
        clearVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cache.getInstance(SettingsActivity.this).clear(Cache.CATEGORY.VIDEO);
                showWarning(getString(R.string.setting_hint_video));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.saveSettings:
                applyAllSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void setAllPrefsDefault() {
        Config.getInstance(this).getAppPrefs().setDefault();
        Config.getInstance(this).getAudioPrefs().setDefault();
        Config.getInstance(this).getDataCollectPrefs().setDefault();
        Config.getInstance(this).getNetworkPrefs().setDefault();
        Config.getInstance(this).getUploadPrefs().setDefault();
        setConfigViewFromStorage();
    }

    // App
    private void readAndSetAppPrefsFromStorage() {
        // read
        boolean appRunBackground = Config.getInstance(this).getAppPrefs().isRunBackground();
        // set
        appRunBackgroundSwitch.setChecked(appRunBackground);
    }
    private boolean getAndWriteAppPrefsToStorage() {
        // get
        boolean appRunBackground = appRunBackgroundSwitch.isChecked();
        // write
        // always SUCCESS
        Config.getInstance(this).getAppPrefs().setRunBackground(appRunBackground);
        return true;
    }

    // Audio
    private void readAndSetAudioPrefsFromStorage() {
        // read
        float audioRecordTime   = Config.getInstance(this).getAudioPrefs().getAudioLength();
        float audioRecordPeriod = Config.getInstance(this).getAudioPrefs().getAudioPeriod();
        boolean runBackground   = Config.getInstance(this).getAudioPrefs().isRunBackground();
        // set
        audioRecordTimeInput.setText("" + audioRecordTime);
        audioRecordPeriodInput.setText("" + audioRecordPeriod);
        audioSwitch.setChecked(runBackground);
    }
    private boolean getAndWriteAudioPrefsToStorage() {
        // get
        float audioRecordTime   = Float.parseFloat(audioRecordTimeInput.getText().toString());
        float audioRecordPeriod = Float.parseFloat(audioRecordPeriodInput.getText().toString());
        if (audioRecordTime > audioRecordPeriod) {
            showWarning("Audio Record Period must be greater than Audio Length!");
            return false;
        }
        boolean runBackground = audioSwitch.isChecked();
        // write
        String result = Config.getInstance(this).getAudioPrefs().setAudioLength(audioRecordTime);
        if(result != Config.SUCCESS_FLAG) {
            showWarning(result);
            return false;
        }
        result = Config.getInstance(this).getAudioPrefs().setAudioPeriod(audioRecordPeriod);
        if(result != Config.SUCCESS_FLAG) {
            showWarning(result);
            return false;
        }
        // always SUCCESS
        Config.getInstance(this).getAudioPrefs().setRunBackground(runBackground);
        return true;
    }

    // DataCollect
    private void readAndSetDataCollectPrefsFromStorage() {
        // read
        boolean runService              = Config.getInstance(this).getDataCollectPrefs().isRunService();
        float collectPeriod             = Config.getInstance(this).getDataCollectPrefs().getCollectPeriod();
        int accelerometerSampleCount    = Config.getInstance(this).getDataCollectPrefs().getAccelerometerSampleCount();
        float accelerometerSampleTime   = Config.getInstance(this).getDataCollectPrefs().getAccelerometerSampleTime();
        // set
        dataCollectSwitch.setChecked(runService);
        dataCollectPeriodInput.setText("" + collectPeriod);
        accelerometerSampleCountInput.setText("" + accelerometerSampleCount);
        textView26.setText("" + accelerometerSampleCount);
        accelerometerSampleTimeInput.setText("" + accelerometerSampleTime);
    }
    private boolean getAndWriteDataCollectPrefsToStorage() {
        // get
        boolean runService              = dataCollectSwitch.isChecked();
        float collectPeriod             = Float.parseFloat(dataCollectPeriodInput.getText().toString());
        int accelerometerSampleCount    = Integer.parseInt(accelerometerSampleCountInput.getText().toString());
        float accelerometerSampleTime   = Float.parseFloat(accelerometerSampleTimeInput.getText().toString());
        if(accelerometerSampleTime > collectPeriod) {
            showWarning("Data Collect Period must be greater than Accelerometer Sample Time!");
            return false;
        }
        // write
        String result = Config.getInstance(this).getDataCollectPrefs().setCollectPeriod(collectPeriod);
        if(result != Config.SUCCESS_FLAG) {
            showWarning(result);
            return false;
        }
        result = Config.getInstance(this).getDataCollectPrefs().setAccelerometerSampleCount(accelerometerSampleCount);
        if(result != Config.SUCCESS_FLAG) {
            showWarning(result);
            return false;
        }
        result = Config.getInstance(this).getDataCollectPrefs().setAccelerometerSampleTime(accelerometerSampleTime);
        if(result != Config.SUCCESS_FLAG) {
            showWarning(result);
            return false;
        }
        // always SUCCESS
        Config.getInstance(this).getDataCollectPrefs().setRunService(runService);
        return true;
    }

    // Network
    private void readAndSetNetworkPrefsFromStorage() {
        // read
        String serverAddress = Config.getInstance(this).getNetworkPrefs().getServerAddress();
        // set
        serverAddressInput.setText(serverAddress);
    }
    private boolean getAndWriteNetworkPrefsToStorage() {
        // get
        String severAddress = serverAddressInput.getText().toString();
        // write
        // always SUCCESS
        Config.getInstance(this).getNetworkPrefs().setServerAddress(severAddress);
        return true;
    }

    // Upload
    private void readAndSetUploadPrefsFromStorage() {
        // read
        boolean runBackground   = Config.getInstance(this).getUploadPrefs().isRunBackground();
        float uploadPeriod      = Config.getInstance(this).getUploadPrefs().getUploadPeriod();
        int maxUploadCount      = Config.getInstance(this).getUploadPrefs().getMaxUploadCount();
        int strategyCode        = Config.getInstance(this).getUploadPrefs().getStrategyCode();
        // set
        uploadBackgroundSwitch.setChecked(runBackground);
        uploadPeriodInput.setText("" + uploadPeriod);
        maxUploadCountInput.setText("" + maxUploadCount);
        uploadStrategySpinner.setSelection(strategyCode);
    }
    private boolean getAndWriteUploadPrefsToStorage() {
        // get
        boolean runBackground   = uploadBackgroundSwitch.isChecked();
        float uploadPeriod      = Float.parseFloat(uploadPeriodInput.getText().toString());
        int maxUploadCount      = Integer.parseInt(maxUploadCountInput.getText().toString());
        int strategyCode        = uploadStrategySpinner.getSelectedItemPosition();
        // write
        String result = Config.getInstance(this).getUploadPrefs().setUploadPeriod(uploadPeriod);
        if(!result.equals(Config.SUCCESS_FLAG)) {
            showWarning(result);
            return false;
        }
        result = Config.getInstance(this).getUploadPrefs().setMaxUploadCount(maxUploadCount);
        if(!result.equals(Config.SUCCESS_FLAG)) {
            showWarning(result);
            return false;
        }
        // always SUCCESS
        Config.getInstance(this).getUploadPrefs().setRunbackground(runBackground);
        Config.getInstance(this).getUploadPrefs().setStrategyCode(strategyCode);
        return true;
    }

    private void setConfigViewFromStorage() {
        readAndSetAppPrefsFromStorage();
        readAndSetAudioPrefsFromStorage();
        readAndSetDataCollectPrefsFromStorage();
        readAndSetNetworkPrefsFromStorage();
        readAndSetUploadPrefsFromStorage();
    }

    private boolean writeConfigToStorage() {
        if(!getAndWriteAppPrefsToStorage()) {
            readAndSetAppPrefsFromStorage();
            appCheckView.setVisibility(View.GONE);
            return false;
        } else {
            appCheckView.setVisibility(View.VISIBLE);
        }

        if(!getAndWriteAudioPrefsToStorage()) {
            readAndSetAudioPrefsFromStorage();
            audioCheckView.setVisibility(View.GONE);
            return false;
        } else {
            audioCheckView.setVisibility(View.VISIBLE);
        }

        if(!getAndWriteDataCollectPrefsToStorage()) {
            readAndSetDataCollectPrefsFromStorage();
            dataCheckView.setVisibility(View.GONE);
            return false;
        } else {
            dataCheckView.setVisibility(View.VISIBLE);
        }

        if(!getAndWriteNetworkPrefsToStorage()) {
            readAndSetNetworkPrefsFromStorage();
            networkCheckView.setVisibility(View.GONE);
            return false;
        } else {
            networkCheckView.setVisibility(View.VISIBLE);
        }

        if(!getAndWriteUploadPrefsToStorage()) {
            readAndSetUploadPrefsFromStorage();
            uploadCheckView.setVisibility(View.GONE);
            return false;
        } else {
            uploadCheckView.setVisibility(View.VISIBLE);
        }

        return true;
    }

    private void applyAllSettings() {
        if(writeConfigToStorage()) { // set values properly
            setResult(RESULT_OK);
            finish();
        }
    }

    private void testConnection() {
        // UI
        final String lastButtonText = testConnectionButton.getText().toString(); // most likely to be "TEST CONNECTION"
        testConnectionButton.setText(getString(R.string.setting_button_testconnection1));
        testConnectionButton.setEnabled(false);

        final String query = "?magicKey=" + MyApplication.MAGIC_CONNECTION_KEY;
        String serverAddress = serverAddressInput.getText().toString();
        Request.Builder requestBuilder = new Request.Builder();
        Request request = null;

        // check url
        try {
            request = requestBuilder
                    .url(serverAddress + query)
                    .build();
        } catch (IllegalArgumentException e) {
            // if url is not a valid HTTP or HTTPS URL.
            showWarning("Not a valid HTTP/HTTPS url\n" + e.getMessage(), true);
        } catch (Exception e) {
            showWarning(e.getMessage(), true);
        }
        if(request == null) {
            // UI
            testConnectionButton.setText(lastButtonText);
            testConnectionButton.setEnabled(true);
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS) // timeout 5s
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // UI
                        testConnectionButton.setText(lastButtonText);
                        testConnectionButton.setEnabled(true);
                        // timeout or something
                        showWarning(e.getMessage(), true);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // UI
                        testConnectionButton.setText(lastButtonText);
                        testConnectionButton.setEnabled(true);

                        if(response.code() == 200) {
                            // get response body
                            String body = null;
                            try {
                                body = response.body().string();
                            } catch (IOException e) {}

                            /*
                            * this is to check if the server is designed for this app only
                            * but not a general HTTP server, sample code of a server may be like this:
                            *
                            * def GET(self):
		                    *   try:
			                *       if web.input()['magicKey'] == MAGIC_CONNECTION_KEY:
				            *           print 'A device is connected!'
				            *           return MAGIC_CONNECTION_KEY
			                *       else:
				            *           print web.input()['magicKey']
		                    *   except Exception as e:
			                *       print(e.what())
		                    *   return 'Invalid Identification'
                            */
                            String hint = null;
                            if(body == null || !body.equals(MyApplication.MAGIC_CONNECTION_KEY)) {
                                hint = "Connection is OK, but server failed to response correctly";
                            } else {
                                // body.equals(MyApplication.MAGIC_CONNECTION_KEY) is true
                                hint = "SUCCESS!";
                            }
                            showWarning(hint, true);
                        } else {
                            // something wrong with the sever
                            showWarning(
                                    "Something wrong with the server\nHTTP status code: " + response.code(),
                                    true
                            );
                        }
                    }
                });
            }
        });
    }
}