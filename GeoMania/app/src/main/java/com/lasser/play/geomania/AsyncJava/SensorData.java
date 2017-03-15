package com.lasser.play.geomania.AsyncJava;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.security.Provider;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by antar on 06-Feb-17.
 */

public class SensorData implements SensorEventListener{
    Context mContext;

    SensorManager sensorManager;
    Sensor sensor_proximity, sensor_accelerometer, sensor_gyroscope, sensor_magnetic, sensor_pressure;
    public float accelerometer[] = new float[3], gyroscope[] = new float[3], pressure, magnetic[]= new float[3], proximity;

    public SensorData(Context context) {
        this.mContext = context;
        // Sensor Data
        sensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
        sensor_proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor_proximity, SensorManager.SENSOR_DELAY_NORMAL);
        sensor_accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor_accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensor_gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, sensor_gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensor_magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, sensor_magnetic, SensorManager.SENSOR_DELAY_NORMAL);
        sensor_pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(this, sensor_pressure, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("MYAPP","Successfully initialized all the sensors!");
    }
    public void Unregister(){
        sensorManager.unregisterListener(this,sensor_accelerometer);
        sensorManager.unregisterListener(this,sensor_gyroscope);
        sensorManager.unregisterListener(this,sensor_magnetic);
        sensorManager.unregisterListener(this,sensor_pressure);
        sensorManager.unregisterListener(this,sensor_proximity);
        Log.d("MYAPP","Unregistered Sensors!");
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            accelerometer[0] = sensorEvent.values[0];
            accelerometer[1] = sensorEvent.values[1];
            accelerometer[2] = sensorEvent.values[2];
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY){
            proximity = sensorEvent.values[0];
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gyroscope[0] = sensorEvent.values[0];
            gyroscope[1] = sensorEvent.values[1];
            gyroscope[2] = sensorEvent.values[2];
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            magnetic[0] = sensorEvent.values[0];
            magnetic[1] = sensorEvent.values[1];
            magnetic[2] = sensorEvent.values[2];
        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
            pressure = sensorEvent.values[0];
        }
        //if(mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        //    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
