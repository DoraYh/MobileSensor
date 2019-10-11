package com.qianzuncheng.sensors.Network;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import com.qianzuncheng.sensors.MyApplication;
import com.qianzuncheng.sensors.R;
import com.qianzuncheng.sensors.Storage.Cache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    static final long MAX_LOG_FILE_SIZE = 100 * 1024; // 100 Kb

    // <timestamp>/<event>/<detail>
    public static class Entry {
        private String timestamp;
        private String event;
        private String detail;
        public Entry(@Nullable String s) {
            if(s != null) parse(s);
        }
        public void parse(String s) {
            try {
                String[] t = s.split("/");
                timestamp   = t[0];
                event       = t[1];
                detail      = t[2];
            } catch (Exception e) {
                android.util.Log.e("Log.Entry", "parse: " + e.toString());
            }

        }
        public SpannableString toSpannableString() {
            String[] showStr = {
                    "", "  ",
                    "", "  \n" + MyApplication.getContext().getString(R.string.main_list_file) + ": ",
                    "", "  "
            };
            Date date = new Date(Long.parseLong(timestamp));
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            // formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateFormatted = formatter.format(date);
            showStr[0] = dateFormatted;
            showStr[2] = event;
            String[] t = detail.split(";");
            showStr[4] = t[1];
            String finalString = "";
            for(String e: showStr) {
                finalString += e;
            }
            SpannableString spannableString = new SpannableString(finalString);
            // bold
            spannableString.setSpan(
                    new StyleSpan(android.graphics.Typeface.BOLD),
                    finalString.indexOf(showStr[3]),
                    finalString.indexOf(showStr[3]) + showStr[3].length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            return spannableString;
        }
    }

    private static void append(String event, String detail, Context context) {
        // clear log if it's to big
        File files[] = Cache.getInstance(context).getFiles(Cache.CATEGORY.NETWORK_LOG);
        if(!(files == null) && files.length > 0) {
            File logFile = files[0];
            if(logFile.length() > MAX_LOG_FILE_SIZE) {
                Cache.getInstance(context).clear(Cache.CATEGORY.NETWORK_LOG);
            }
        }

        long time = System.currentTimeMillis();
        String entry = String.format("%s/%s/%s", time, event, detail);
        Cache.getInstance(context).append(Cache.CATEGORY.NETWORK_LOG, entry);
    }

    public static void uploading(String detail, Context context) {
        append(UploadService.STATUS.UPLOADING, detail, context);
    }
    public static void uploadFailed(String detail, Context context) {
        append(UploadService.STATUS.UPLOAD_FAILED, detail, context);
    }
    public static void uploadSuccess(String detail, Context context) {
        append(UploadService.STATUS.UPLOAD_SUCCESS, detail, context);
    }
    public static void idle(Context context) {
        // append(UploadService.STATUS.IDLE, " ", context);
    }
}
