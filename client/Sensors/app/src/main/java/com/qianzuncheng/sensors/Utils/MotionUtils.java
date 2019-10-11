package com.qianzuncheng.sensors.Utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionUtils implements SensorEventListener {
    public static final String TAG = "MotionUtils";

    private volatile static MotionUtils uniqueInstance;
    private Context mContext;

    private SensorManager sensorManager;
    private double[] values = {0, 0, 0};
    private String valueString = "";

    private MotionUtils(Context context) {
        mContext = context;
        //updateMotion();
    }

    // Double CheckLock(DCL)
    public static MotionUtils getInstance(Context context) {
        if (uniqueInstance == null) {
            synchronized (MotionUtils.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new MotionUtils(context);
                }
            }
        }
        return uniqueInstance;
    }

    public void registerSensor() {
        valueString = "";
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    public void recordValues() {
        this.valueString += String.format(
                "x:%.2f y:%.2f z:%.2f;",
                values[0], values[1], values[2]
        );
    }

    public String getValueString() {
        return valueString;
    }

    public void removeMotionUpdatesListener() {
        valueString = "";
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            values[0] = sensorEvent.values[0];
            values[1] = sensorEvent.values[1];
            values[2] = sensorEvent.values[2];
        }
    }
}
