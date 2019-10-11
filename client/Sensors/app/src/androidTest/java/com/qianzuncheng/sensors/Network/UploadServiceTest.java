package com.qianzuncheng.sensors.Network;

import android.content.Context;
import android.content.Intent;

import com.qianzuncheng.sensors.MyApplication;

import org.junit.Test;

public class UploadServiceTest {
    @Test
    public void duplicateUpload() {
        Context context = MyApplication.getContext();
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra("test", true);
        context.startService(intent);
    }
}