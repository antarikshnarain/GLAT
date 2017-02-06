package com.lasser.play.geomania;

import android.Manifest;
import android.content.Context;
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
import android.widget.Toast;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by antar on 06-Feb-17.
 */

public class SensorData {
    Context mContext;
    SensorManager sensorManager;
    LocationManager locationManager;
    Sensor sensor_proximity, sensor_accelerometer, sensor_gyroscope, sensor_magnetic, sensor_pressure;
    public float accelerometer[] = new float[3], gyroscope[] = new float[3], pressure, magnetic[]= new float[3], proximity;
    public double gps_longi, gps_lati;
    final int t= 3000, distance = 5;
    final SensorEventListener mySensorEventListener = new SensorEventListener() {
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
    };
    LocationListener myLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    public void updateWithNewLocation(Location location){
        if(location!=null) {
            gps_lati = location.getLatitude();
            gps_longi = location.getLongitude();
        }
    }
    public SensorData(Context context){
        this.mContext = context;
        // Sensor Data
        sensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
        sensor_proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(mySensorEventListener,sensor_proximity,SensorManager.SENSOR_DELAY_NORMAL);
        sensor_accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(mySensorEventListener,sensor_accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        sensor_gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(mySensorEventListener,sensor_gyroscope,SensorManager.SENSOR_DELAY_NORMAL);
        sensor_magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(mySensorEventListener,sensor_magnetic,SensorManager.SENSOR_DELAY_NORMAL);
        sensor_pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(mySensorEventListener,sensor_pressure,SensorManager.SENSOR_DELAY_NORMAL);

        //Location Data
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        String provider = locationManager.getBestProvider(criteria,true);
        if(mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            Location location = locationManager.getLastKnownLocation(provider);
            updateWithNewLocation(location);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, t, distance, myLocationListener);
        }
        else{
            Toast.makeText(mContext.getApplicationContext(),"GPS Permission Missing", Toast.LENGTH_SHORT).show();
        }
        //
        //    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


    }
    public SensorData() {

    }
    public void Unregister(){
        sensorManager.unregisterListener(mySensorEventListener,sensor_accelerometer);
        sensorManager.unregisterListener(mySensorEventListener,sensor_gyroscope);
        sensorManager.unregisterListener(mySensorEventListener,sensor_magnetic);
        sensorManager.unregisterListener(mySensorEventListener,sensor_pressure);
        sensorManager.unregisterListener(mySensorEventListener,sensor_proximity);
    }
    /*
    Calling From another Activity
    mydata = new SensorData(this.getApplicationContext());
    public void updateData(View v){
        String data="";
        data+="Accelerometer:\n" + mydata.accelerometer[0] + "," + mydata.accelerometer[1] + ","+ mydata.accelerometer[2];
        data+="\n\nGyroscope:\n" + mydata.gyroscope[0] + "," + mydata.gyroscope[1] + "," + mydata.gyroscope[2];
        data+="\n\nMagnetic:\n" + mydata.magnetic[0] + "," + mydata.magnetic[1] + "," + mydata.magnetic[2];
        data+="\n\nPressure:\n" + mydata.pressure;
        data+="\n\nProximity:\n" + mydata.proximity;
        data+="\n\n\nLocation:\n" + mydata.gps_lati + "," + mydata.gps_longi;
        myview.setText(data);
    }
    public void releaseSensors(View v){
        mydata.Unregister();
    }
     */
}
