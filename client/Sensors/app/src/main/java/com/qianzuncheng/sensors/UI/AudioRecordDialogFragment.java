package com.qianzuncheng.sensors.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;

import com.melnykov.fab.FloatingActionButton;
import com.qianzuncheng.sensors.MainActivity;
import com.qianzuncheng.sensors.R;
import com.qianzuncheng.sensors.Utils.AudioUtils;
/*

                                 WARNING

              This class maybe useful in the future, but NOT now

                             IGNORE it for now












*/
public class AudioRecordDialogFragment extends DialogFragment {
    private Context context;

    private boolean startRecording = true;

    long timeWhenPaused = 0;

    private FloatingActionButton fabRecord;
    private Chronometer chronometerTime;
    private ImageView ivClose;
    private OnAudioCancelListener listener;

    public void setContext(Context context) {
        this.context = context;
    }

    public static AudioRecordDialogFragment newInstance(Context context) {
        AudioRecordDialogFragment dialogFragment = new AudioRecordDialogFragment();
        dialogFragment.setContext(context);
        // Bundle bundle = new Bundle();
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_record_audio, null);
        chronometerTime     = view.findViewById(R.id.record_audio_chronometer_time);
        fabRecord           = view.findViewById(R.id.record_audio_fab_record);
        ivClose             = view.findViewById(R.id.record_audio_iv_close);

        fabRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                // Stop background recording & cancel timer
                if(MyApplication.audioRecordServiceIntent != null) {
                    MyApplication.getContext().stopService(MyApplication.audioRecordServiceIntent);
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.stopAudioRecordTimer();
                }
                onRecord(startRecording);
                startRecording = !startRecording;
                */
            }
        });

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.resumeAudioRecordTimer();
            }
        });

        builder.setCancelable(false);
        builder.setView(view);
        return builder.create();
    }

    private void onRecord(boolean start) {
        Intent intent = new Intent(getActivity(), AudioUtils.class);
        if (start) {
            chronometerTime.setBase(SystemClock.elapsedRealtime());
            chronometerTime.start();
            getActivity().startService(intent);
        } else {
            chronometerTime.stop();
            timeWhenPaused = 0;
            getActivity().stopService(intent);
        }
    }

    public void stopRecord() {
        Intent intent = new Intent(getActivity(), AudioUtils.class);
        chronometerTime.stop();
        timeWhenPaused = 0;
        getActivity().stopService(intent);
    }

    public void setOnCancelListener(OnAudioCancelListener listener) {
        this.listener = listener;
    }

    public interface OnAudioCancelListener {
        void onCancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    onRecord(startRecording);
                }
                break;
        }
    }
}
