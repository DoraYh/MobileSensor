package com.qianzuncheng.sensors.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.qianzuncheng.sensors.R;

public class AppInfoDialogFragment extends DialogFragment {
    private Context context;
    OnAppInfoCancelListener listener;

    public void setContext(Context context) {
        this.context = context;
    }

    public static AppInfoDialogFragment newInstance(Context context) {
        AppInfoDialogFragment dialogFragment = new AppInfoDialogFragment();
        dialogFragment.setContext(context);
        // Bundle bundle = new Bundle();
        return dialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_app_info, null);
        ImageView closeButton = view.findViewById(R.id.app_info_close);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
            }
        });

        builder.setCancelable(false);
        builder.setView(view);
        return builder.create();
    }

    public void setOnCancelListener(OnAppInfoCancelListener listener) {
        this.listener = listener;
    }

    public interface OnAppInfoCancelListener {
        void onCancel();
    }
}
