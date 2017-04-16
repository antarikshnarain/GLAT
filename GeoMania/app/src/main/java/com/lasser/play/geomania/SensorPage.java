package com.lasser.play.geomania;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.GpsData;
import com.lasser.play.geomania.AsyncJava.SensorData;

import java.util.ArrayList;

public class SensorPage extends AppCompatActivity{

    ArrayList<String> list = new ArrayList<String>();
    CustomGroupListAdapter_SensorList adapter;
    String[] itemTitle={"Accelerometer","Gyroscope","Magnetic","Pressure","Proximity","Location"};
    String[] itemContent=new String[6];
    ListView myList;
    SensorData sensorData;
    GpsData gpsData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},101);
            Log.d("MYAPP","Location Manager Successfully Created");
        }
        sensorData = new SensorData(this);
        gpsData = new GpsData(this);
        adapter = new CustomGroupListAdapter_SensorList(this,itemTitle,itemContent,R.drawable.ic_launcher);
        myList = (ListView) findViewById(R.id.groupListView);
        myList.setAdapter(adapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String SelectedItem = itemTitle[+position];
                Toast.makeText(getApplicationContext(),SelectedItem,Toast.LENGTH_SHORT).show();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Sensor Data Updated", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                //updateData();
                gpsData.getLocation();
            }
        });
        final Handler h = new Handler();
        final int delay = 1000; //milliseconds

        h.postDelayed(new Runnable(){
            public void run(){
                //do something
                updateData();
                // To update the list
                adapter.notifyDataSetChanged();
                h.postDelayed(this, delay);
            }
        }, delay);
    }
    public void updateData(){
        itemContent[0] = sensorData.accelerometer[0] + ", " + sensorData.accelerometer[1] + ", "+ sensorData.accelerometer[2];
        itemContent[1] = sensorData.gyroscope[0] + ", " + sensorData.gyroscope[1] + ", " + sensorData.gyroscope[2];
        itemContent[2] = sensorData.magnetic[0] + ", " + sensorData.magnetic[1] + ", " + sensorData.magnetic[2];
        itemContent[3] = ""+sensorData.pressure;
        itemContent[4] = ""+sensorData.proximity;
        itemContent[5] = gpsData.gps_latitude + ", " + gpsData.gps_longitude;
    }
    public void onResume(){
        super.onResume();
        sensorData = new SensorData(this);
        gpsData = new GpsData(this);
    }
    public void onPause(){
        super.onPause();
    }
    public void onStop(){
        super.onStop();
        Log.d("MYAPP", "App Stopped");
        sensorData.Unregister();
    }
}
