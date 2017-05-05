package com.lasser.play.geomania;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.lasser.play.geomania.AsyncJava.GpsData;
import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;
import com.lasser.play.geomania.ListAdapter.CustomGroupListAdapter_GroupView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.json.JSONObject.NULL;

public class GroupView extends AppCompatActivity implements LocationListener {
    ListView myList;
    SearchView mSearchView;

    SharedFunctions myfunction;
    boolean first_time=false;
    ProgressDialog progressDialog;
    // Location Variables
    LocationManager locationManager;
    Location location;
    final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // 10 Meters
    final long MIN_TIME_BW_UPDATES = 1000 * 2 * 1; // 1 minute
    private double gps_latitude, gps_longitude;

    // Custom List Adapter
    ArrayList<String> list = new ArrayList<String>();
    CustomGroupListAdapter_GroupView adapter;
    ArrayList<String> groupId = new ArrayList<String>();
    ArrayList<String> groupName = new ArrayList<String>();
    ArrayList<String> groupIcon = new ArrayList<String>();
    ArrayList<String> groupUnread = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);
        // Get All permissions
        if(checkSelfPermission(Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION}, 202);
        }
        //GpsData gpsData = new GpsData(this);
        Log.d("MYAPP: GPS", "Done with GPS");
        mSearchView = (SearchView) findViewById(R.id.groupsearchview);
        myList = (ListView) findViewById(R.id.showGroups);
        myfunction = new SharedFunctions(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(":((())):");
        progressDialog.setMessage("Waiting for location ...");
        progressDialog.show();
        // calls getGroupData in get Location
        getLocation();
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if(!first_time){
                first_time = true;
                progressDialog.setMessage("Fetching Group Data ...");
                try {
                    requestGroupData();
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
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
        Toast.makeText(this,"Provider Enabled"+provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MYAPP", "Location Manager Successfully Created");
            }
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                // First get location from Network Provider
                //locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000, 5, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, this);
                Criteria criteria = new Criteria();
                //criteria.setAccuracy(Criteria.ACCURACY_FINE);
                //criteria.setPowerRequirement(Criteria.POWER_HIGH);
                //criteria.setAltitudeRequired(true);
                String provider = locationManager.getBestProvider(criteria, false);
                Toast.makeText(this.getApplicationContext(), "Current Provider: " + provider, Toast.LENGTH_SHORT).show();
                //locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(provider);
                    if (location != null) {
                        Log.d("MYAPP", "Loading Last known location!");
                        gps_latitude = location.getLatitude();
                        gps_longitude = location.getLongitude();
                        progressDialog.setMessage("Fetching Group Data ...");
                        try {
                            requestGroupData();
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                        first_time = true;
                    } else {
                        Toast.makeText(this, "Last Location Unknown", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("MYAPP", "Location Manager NULL");
                }
                Log.d("MYAPP", "Location Manager Initialized!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void requestGroupData() throws JSONException{
        JSONObject requestMap = new JSONObject();
        requestMap.put("phone", myfunction.phone);
        requestMap.put("token", myfunction.token);
        requestMap.put("lat",Double.toString(gps_latitude));
        requestMap.put("long",Double.toString(gps_longitude));
        Log.d("MYAPP: RequestData",requestMap.toString());
        Log.d("MYAPP: SharedPrefs", myfunction.phone + " " + myfunction.token);
        URLDataHash mydata = new URLDataHash();
        mydata.url = myfunction.serverUrl;
        mydata.apicall = "user/groupListView";
        mydata.jsonData=requestMap;
        try {
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if(data == null){
                Log.d("MYAPP: ServerResp","Error during server request");
                ArrayList<String> t_gid, t_name, t_icon, t_unread;
                t_gid = new ArrayList<>();
                t_name = new ArrayList<String>();
                t_icon = new ArrayList<String>();
                t_unread = new ArrayList<String>();
                t_gid.add("1");
                t_name.add("Alpha");
                t_icon.add("");
                t_unread.add("Unread: 23");
                adapter = new CustomGroupListAdapter_GroupView(this,t_gid, t_name,t_icon,t_unread);
                myList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                return;
            }
            JSONArray groups = data.getJSONArray("resp");
            JSONObject currentObj;
            for(int i=0;i<groups.length();i++)
            {
                currentObj = groups.getJSONObject(i);
                groupName.add(i,currentObj.getString("gname"));
                groupId.add(i, currentObj.getString("gid"));
                groupUnread.add(i, "Unread: " + currentObj.getString("unread_count"));
                groupIcon.add(i, currentObj.getString("pic_location"));
            }
            adapter = new CustomGroupListAdapter_GroupView(this,groupId, groupName,groupIcon,groupUnread);
            myList.setAdapter(adapter);
        }
        catch (InterruptedException e){ e.printStackTrace(); }
        catch (ExecutionException e){ e.printStackTrace(); }
    }
    // Creating a New Group
    public void addGroup(View v){
        Intent new_group_intent = new Intent().setClass(this,GroupManager.class);
        new_group_intent.putExtra("title","");
        new_group_intent.putExtra("icon","");
        new_group_intent.putExtra("gid","");
        startActivity(new_group_intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_view, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editprofile:
                // About option clicked.
                Intent intent_edit_profile = new Intent().setClass(this, EditProfile.class);
                startActivity(intent_edit_profile);
                return true;
            case R.id.action_refresh_group:
                Toast.makeText(getApplicationContext(), "Refresh Group List", Toast.LENGTH_SHORT).show();
                progressDialog.setMessage("Fetching Group Data ...");
                progressDialog.show();
                adapter.clear();
                try{
                    requestGroupData();
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                return true;
            case R.id.action_searchgroup:
                Toast.makeText(getApplicationContext(), "Search Groups", Toast.LENGTH_SHORT).show();
                if (mSearchView.getVisibility() == View.VISIBLE) {
                    mSearchView.setVisibility(View.GONE);
                } else {
                    mSearchView.setVisibility(View.VISIBLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onRequestPermissionsResult (int requestCode, String permission[], int[] grantResults){
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

}
