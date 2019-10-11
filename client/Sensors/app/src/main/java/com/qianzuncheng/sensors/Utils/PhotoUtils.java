package com.qianzuncheng.sensors.Utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.qianzuncheng.sensors.MainActivity;
import com.qianzuncheng.sensors.MyApplication;

import java.io.File;
import java.io.IOException;

public class PhotoUtils {
    public static final String dirName = "Sensors";

    private static final String FILE_PROVIDER_AUTHORITY = "com.qianzuncheng.sensors.fileprovider";
    private static final String filenamePrefix = "photo";

    public static void takeOneShot(Activity activity) {
        // check dir
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + dirName + "/";
        File newdir = new File(dir);
        if(!newdir.exists()) {
            newdir.mkdirs();
        }
        // create file
        long startingTimeMillis = System.currentTimeMillis();
        String filePath = String.format("%s%s_%s.jpg",
                dir, filenamePrefix, startingTimeMillis);
        File newFile = new File(filePath);
        try {
            newFile.createNewFile();
        } catch (IOException e) {}
        // pull up camera activity
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {// prevent app crash when no camera detected
            Uri outputFileUri = Uri.fromFile(newFile);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                outputFileUri = FileProvider.getUriForFile(activity, FILE_PROVIDER_AUTHORITY, newFile);
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            MyApplication.lastTakenPhotoPath = filePath;
            activity.startActivityForResult(cameraIntent, MainActivity.TAKE_PHOTO_CODE);
        }
    }
}
