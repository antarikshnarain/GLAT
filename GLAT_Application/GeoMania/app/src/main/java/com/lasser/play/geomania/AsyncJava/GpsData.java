package com.lasser.play.geomania.AsyncJava;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.lasser.play.geomania.MapsActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Provider;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by antar on 06-Feb-17.
 */

public class GpsData implements LocationListener{
    Context mContext;
    boolean isGPSEnabled = false,isNetworkEnabled = false, canGetLocation = false;
    Location location;
    public double gps_longitude=0.0, gps_latitude=0.0;
    public String provider;
    final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 Meters
    final long MIN_TIME_BW_UPDATES = 1000 * 2 * 1; // 1 minute
    protected LocationManager locationManager;

    private ProgressDialog progressDialog;

    // Writing to File
    String filename = "MyGPSData.csv";
    FileOutputStream outputStream;
    File sdcard = Environment.getExternalStorageDirectory();
    // to this path add a new directory path
    File dir = new File(sdcard.getAbsolutePath() + "/GeoMania/");
    public GpsData(Context context){
        this.mContext = context;
        progressDialog = new ProgressDialog(context);
        getLocation();
    }
    public void getLocation(){
        try {
            progressDialog.setTitle(":((())):");
            progressDialog.setMessage("Waiting for location ...");
            progressDialog.show();
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MYAPP","Location Manager Successfully Created");
            }
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAltitudeRequired(true);
                criteria.setBearingRequired(true);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);
                provider = locationManager.getBestProvider(criteria, false);
                //provider = LocationManager.PASSIVE_PROVIDER;
                Toast.makeText(mContext.getApplicationContext(),"Current Provider: "+provider, Toast.LENGTH_SHORT).show();
                locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        gps_latitude = location.getLatitude();
                        gps_longitude = location.getLongitude();
                        Toast.makeText(mContext,"Updating with Last Known Location", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Log.d("MYAPP","Location Manager NULL");
                }
                Log.d("MYAPP","Location Manager Initialized!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            progressDialog.dismiss();
            gps_latitude = location.getLatitude();
            gps_longitude = location.getLongitude();
            //MapsActivity mapsActivity = new MapsActivity();
            //mapsActivity.updateMap(gps_latitude,gps_longitude);
            /*
            //Writing data to File
            String data = String.valueOf(gps_latitude)+','+String.valueOf(gps_longitude)+'\n';
            dir.mkdir();
            try {
                File file = new File(dir, filename);
                outputStream = new FileOutputStream(file,true);
                outputStream.write(data.getBytes());
                outputStream.close();
                //Log.d("MYAPP", "Data Written to file successfully");
            }
            catch (Exception e){
                Log.d("MYAPP","File Cannot be written");
            }
            */
        }
        Log.d("MYAPP", "Updating My Location!");
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
