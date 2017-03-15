package com.lasser.play.geomania.AsyncJava;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
    final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    protected LocationManager locationManager;

    public GpsData(Context context){
        this.mContext = context;
        getLocation();
    }
    public void getLocation(){
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                Log.d("MYAPP","Location Manager Successfully Created");
            }
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                Criteria criteria = new Criteria();
                /*
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setSpeedRequired(false);
                criteria.setCostAllowed(true);*/
                provider = locationManager.getBestProvider(criteria, false);
                Toast.makeText(mContext.getApplicationContext(),"Current Provider: "+provider, Toast.LENGTH_SHORT).show();
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        gps_latitude = location.getLatitude();
                        gps_longitude = location.getLongitude();
                    }
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
            gps_latitude = location.getLatitude();
            gps_longitude = location.getLongitude();
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
