package com.lasser.play.geomania;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
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

public class GroupView extends AppCompatActivity implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    ListView myList;
    SearchView mSearchView;

    SharedFunctions myfunction;
    boolean first_time=false;
    ProgressDialog progressDialog;
    // Location Variables
    LocationManager locationManager;
    Location location;
    private double gps_latitude, gps_longitude;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
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
        LocationManager l = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if(!l.isProviderEnabled(LocationManager.GPS_PROVIDER))
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        if(checkSelfPermission(Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA}, 202);
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
    public void getLocation() {
        try {
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MYAPP", "Location Manager Successfully Created");
            }
            if(googleApiClient == null){
                googleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
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
    public void createLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("MYAPP", "Location Manager Successfully Created");
        }
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            Log.d("MYAPP: Location", "Loading last known location GOOGLE");
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
        }
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
            Log.d("MYAPP: UpdateLocation", ""+gps_latitude+","+gps_longitude);
        }
        Log.d("MYAPP", "Updating My Location!");
    }
    @Override
    protected  void onStart(){
        googleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop(){
        if(googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onStop();

    }
    @Override
    protected void onPause(){
        if(googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 201);
            }
            createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        }
    }
}
