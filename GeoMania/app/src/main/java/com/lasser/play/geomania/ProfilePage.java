package com.lasser.play.geomania;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ProfilePage extends AppCompatActivity{

    ArrayList<String> list = new ArrayList<String>();
    CustomGroupListAdapter adapter;
    String[] itemTitle={"Accelerometer","Gyroscope","Magnetic","Pressure","Proximity","Location"};
    String[] itemContent=new String[6];
    ListView myList;
    SensorData sensorData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sensorData = new SensorData(this);
        adapter = new CustomGroupListAdapter(this,itemTitle,itemContent,R.drawable.ic_launcher);
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
                updateData();
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
        itemContent[5] = sensorData.gps_lati + ", " + sensorData.gps_longi;
    }
    public void releaseSensors(View v){
        sensorData.Unregister();
    }
    public void onPause(){
        super.onPause();
        sensorData.Unregister();
    }
    public void onStop(){
        super.onStop();
        sensorData.Unregister();
    }
}
