package com.lasser.play.geomania;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lasser.play.geomania.AsyncJava.GpsData;
import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.MapMessages;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;
import com.lasser.play.geomania.CustomDataStructure.UserSendMessage;
import com.lasser.play.geomania.ListAdapter.CustomGroupListAdapter_GroupView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleMap.OnInfoWindowClickListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    Boolean flag_detect, flag_detected;
    String object_file_name;
    int temp_id=-1;
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    private GoogleMap mMap;
    private Marker myMarker;
    Location location;
    public double gps_longitude=0.0, gps_latitude=0.0;

    // Map Markers
    public ArrayList<Marker> map_messages;
    public ArrayList<MapMessages> messages;
    // Writing to File
    String filename = "MyGPSData.csv";
    FileOutputStream outputStream;
    File sdcard = Environment.getExternalStorageDirectory();
    // to this path add a new directory path
    File dir = new File(sdcard.getAbsolutePath() + "/GeoMania/");

    // Layout variables
    SearchView mSearchView;
    ImageButton imgButton_location, imgButton_no_location, imgButton_location_tag, imgButton_no_location_tag;
    TextView tv_message_type;
    ProgressDialog progressDialog;

    // Data control variables
    String group_name, group_icon, group_id;
    boolean first_flag = true;
    SharedFunctions myfunction;

    FloatingActionButton button_objectDetect;
    // Intents
    Intent location_message_intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        group_name = intent.getStringExtra("title");
        group_icon = intent.getStringExtra("icon");
        group_id = intent.getStringExtra("gid");
        myfunction = new SharedFunctions(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(":((())):");
        progressDialog.setMessage("Getting Location");
        progressDialog.show();
        // Progress required to get group data
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        getLocation();
        mapFragment.getMapAsync(this);

        // Declaring Layout variables
        mSearchView = (SearchView) findViewById(R.id.searchView);
        imgButton_location = (ImageButton) findViewById(R.id.imageButton_location);
        imgButton_location_tag = (ImageButton) findViewById(R.id.imageButton_location_tag);
        imgButton_no_location = (ImageButton) findViewById(R.id.imageButton_no_location);
        imgButton_no_location_tag = (ImageButton) findViewById(R.id.imageButton_no_location_tag);
        tv_message_type = (TextView) findViewById(R.id.textView_message_type);
        button_objectDetect = (FloatingActionButton) findViewById(R.id.livemode);
        button_objectDetect.setVisibility(View.INVISIBLE);
        flag_detect = false;
        flag_detected = false;
        // Initializing messages variable for map
        location_message_intent = new Intent().setClass(this,LocationMessage.class);
        location_message_intent.putExtra("gid", group_id);
        map_messages = new ArrayList<Marker>();
        messages = new ArrayList<MapMessages>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        updateMap(gps_latitude, gps_longitude);
        loadGroupMessages();
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if(first_flag){
                progressDialog.dismiss();
                first_flag = false;
            }
            gps_latitude = location.getLatitude();
            gps_longitude = location.getLongitude();
            updateMap(gps_latitude,gps_longitude);
        }
        Log.d("MYAPP", "Updating My Location!");
    }
    @Override
    public void onInfoWindowClick(Marker marker){
        int id = (int) marker.getTag();
        if(!flag_detected || temp_id != id) {
            try {
                object_file_name = messages.get(id).sensorData.getString("object");
                Log.d("MYAPP: ObjectFile", object_file_name);
                Log.d("MYAPP: SensorDataObj", messages.get(id).sensorData.toString());
                if (!object_file_name.equals("")) {
                    Log.d("MYAPP: ObjectFile", object_file_name);
                    button_objectDetect.setVisibility(View.VISIBLE);
                    flag_detect = true;
                    temp_id = id;
                } else {
                    flag_detect = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(messages.get(id).message_state==0) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Log.d("MYAPP: MarkerState",messages.get(id).message_state+"");
            messages.get(id).message_state=1;
            Log.d("MYAPP: MarkerState",messages.get(id).message_state+"");
        }
        if(flag_detect){
            Toast.makeText(getApplicationContext(),"Detect object first!",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Mark Message as Read
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "group/markMessageRead";
            mydata.jsonData.put("phone", myfunction.phone);
            mydata.jsonData.put("token", myfunction.token);
            mydata.jsonData.put("mid", messages.get(id).gid);
            mydata.jsonData.put("gid", messages.get(id).mid);
            // Request the server
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if (data == null) {
                Log.d("MYAPP: ServerResp", "Error during server request");
                Toast.makeText(getApplicationContext(),"Server Error", Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e) { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
        // Start Message Feed Activity
        if(messages.get(id).message_state==0) {
            map_messages.get(id).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            messages.get(id).message_state=1;
        }
        Intent message_feed_intent = new Intent().setClass(this,MessageViewFeed.class);
        message_feed_intent.putExtra("gid", messages.get(id).gid);
        message_feed_intent.putExtra("mid", messages.get(id).mid);
        message_feed_intent.putExtra("user", marker.getTitle());
        message_feed_intent.putExtra("message", marker.getSnippet());
        message_feed_intent.putExtra("sensorData", messages.get(id).sensorData.toString());
        startActivity(message_feed_intent);
        flag_detected = false;
    }
    public void createLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    public void addMessageToMap(int id, double latitude, double longitude, int message_state, String summary, int gid, int mid, String createdby, JSONObject sensorData ) {
        MapMessages messageFeed = new MapMessages();
        messageFeed.latitude = latitude;
        messageFeed.longitude = longitude;
        messageFeed.summary = summary;
        messageFeed.gid = gid;
        messageFeed.mid = mid;
        messageFeed.createdby = createdby;
        messageFeed.message_state = message_state;
        messageFeed.sensorData = sensorData;
        Log.d("MYAPP SensorData",messageFeed.sensorData.toString());
        // 0-> unread, 1-> read, 2-> mine
        Marker marker = null;
        Log.d("MYAPP: state", ""+message_state);
        if (message_state == 1) {
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        else if (message_state == 0) {
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }
        else if (message_state == 2) {
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        else{
            return;
        }
        marker.setTag(id);
        marker.setTitle(createdby);
        if (summary.length()>40)
            marker.setSnippet(summary.substring(0,40)+"...");
        else
            marker.setSnippet(summary+"...");
        map_messages.add(id,marker);
        messages.add(id,messageFeed);
        Log.d("MYAPP: marker", marker.getTitle() + marker.getPosition().toString());
    }

    public void loadGroupMessages(){
        try{
            progressDialog.setMessage("Retrieving Group Data ...");
            progressDialog.show();
            JSONObject requestMap = new JSONObject();
            requestMap.put("gid", group_id);
            requestMap.put("phone", myfunction.phone);
            requestMap.put("token", myfunction.token);
            requestMap.put("lat",Double.toString(gps_latitude));
            requestMap.put("long",Double.toString(gps_longitude));
            Log.d("MYAPP: RequestData",requestMap.toString());
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "group/showAllMessages";
            mydata.jsonData=requestMap;
            // Request the server
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if(data == null){
                Log.d("MYAPP: ServerResp","Error during server request");
                return;
            }
            progressDialog.dismiss();
            JSONArray groups = data.getJSONArray("resp");
            Log.d("MY APP",groups.toString());
            JSONObject currentObj=new JSONObject();
            for(int i=0;i<groups.length();i++)
            {
                currentObj = groups.getJSONObject(i);
                Log.d("MYAPP: Marker", "adding marker" + currentObj.toString());
                addMessageToMap(i, currentObj.getDouble("lat"), currentObj.getDouble("long"),currentObj.getInt("readStatus"),currentObj.getString("body"), currentObj.getInt("gid"), currentObj.getInt("mid"), currentObj.getString("createdByName"), currentObj.getJSONObject("sensorData"));
            }
            Log.d("MYAPP: MAPS", "Loaded map with markers");
            //progressDialog.dismiss();
        }
        catch (JSONException e){ e.printStackTrace();}
        catch (InterruptedException e){ e.printStackTrace(); }
        catch (ExecutionException e){ e.printStackTrace(); }
    }

    public void updateMap(double latitude,double longitude){
        LatLng myLocation = new LatLng(latitude,longitude);
        if(mMap !=null){
            if(myMarker != null)
                myMarker.remove();
            myMarker=mMap.addMarker(new MarkerOptions().position(myLocation));
                  //  .title("Mylocation").snippet(myLocation.toString()));
            /*
            CircleOptions circleOptions = new CircleOptions();

            circleOptions.center(myLocation);
            circleOptions.radius(2);
            circleOptions.fillColor(-16711936);
            mMap.addCircle(circleOptions);
            //mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location").snippet("AWESOME").icon(vectorToBitmap(R.drawable.ic_launcher, Color.parseColor("#A4C639"))));
            */
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            // Zooming into the map N times.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            Log.d("MYAPP","Updating Map");
        }
        else{
            Log.d("MYAPP","Map object NULL");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_map, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if(requestCode == 301) {
            if (resultCode == myfunction.SUCCESS) {
                Toast.makeText(getApplicationContext(), "Message Send!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Message Not Send!", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 701){
            if(resultCode == myfunction.SUCCESS){
                flag_detect=false;
                flag_detected = true;
            }
            else if(resultCode == myfunction.FAIL) {
                flag_detect = true;
                flag_detected = false;
            }
            else{
                Toast.makeText(getApplicationContext(),"Object not detected!",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editgroup:
                // About option clicked.
                Intent intent_group_manager = new Intent().setClass(this,GroupManager.class);
                //title, icon, gid
                intent_group_manager.putExtra("title",group_name);
                intent_group_manager.putExtra("icon",group_icon);
                intent_group_manager.putExtra("gid",group_id);
                startActivity(intent_group_manager);
                Toast.makeText(getApplicationContext(),"Group Edit",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_exitgroup:
                // Exit option clicked.
                Toast.makeText(getApplicationContext(),"Group Exit",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_viewread:
                Toast.makeText(getApplicationContext(),"View Read",Toast.LENGTH_SHORT).show();
                for(int i=0;i<map_messages.size();i++){
                    if(messages.get(i).message_state != 1){
                        map_messages.get(i).setVisible(false);
                    }
                    else
                        map_messages.get(i).setVisible(true);
                }
                return true;
            case R.id.action_viewunread:
                Toast.makeText(getApplicationContext(),"View Unread",Toast.LENGTH_SHORT).show();
                for(int i=0;i<map_messages.size();i++){
                    if(messages.get(i).message_state != 0){
                        map_messages.get(i).setVisible(false);
                    }
                    else
                        map_messages.get(i).setVisible(true);
                }
                return true;
            case R.id.action_searchmap:
                Toast.makeText(getApplicationContext(),"Filter by user",Toast.LENGTH_SHORT).show();
                for(int i=0;i<map_messages.size();i++){
                    map_messages.get(i).setVisible(true);
                }
                /*
                if(mSearchView.getVisibility() == View.VISIBLE){
                    mSearchView.setVisibility(View.GONE);
                }
                else {
                    mSearchView.setVisibility(View.VISIBLE);
                }
                */
                return true;
            case R.id.action_refresh_map:
                Toast.makeText(getApplicationContext(),"Refresh Done!",Toast.LENGTH_SHORT).show();
                loadGroupMessages();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createMessage(View v){
        location_message_intent.putExtra("longitude",gps_longitude);
        location_message_intent.putExtra("latitude", gps_latitude);
        location_message_intent.putExtra("type",1);
        startActivityForResult(location_message_intent,301);
    }

    public void livevideomode(View v){
        if(flag_detect) {
            Intent i = new Intent().setClass(this, CameraObjectDetection.class);
            i.putExtra("object",object_file_name);
            startActivityForResult(i, 701);
        }
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
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 201);
            }
            createLocationRequest();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

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
            loadGroupMessages();
            progressDialog.dismiss();
            first_flag = true;
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
}

