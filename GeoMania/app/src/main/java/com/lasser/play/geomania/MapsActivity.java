package com.lasser.play.geomania;

import android.*;
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
import android.support.annotation.StringDef;
import android.support.design.widget.Snackbar;
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
import java.util.concurrent.ExecutionException;
import java.util.zip.Inflater;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private Marker myMarker;
    boolean isGPSEnabled = false,isNetworkEnabled = false, canGetLocation = false;
    Location location;
    public double gps_longitude=0.0, gps_latitude=0.0;
    public String provider;
    final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // 10 Meters
    final long MIN_TIME_BW_UPDATES = 1000 * 2 * 1; // 1 minute
    protected LocationManager locationManager;

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

    String phone, token, group_id;
    boolean first_flag = true;

    // Intents
    Intent location_message_intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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

        // Initializing messages variable for map
        Intent intent = getIntent();
        group_id = intent.getStringExtra("id");
        SharedPreferences phoneDetails = getSharedPreferences("userdata", MODE_PRIVATE);
        phone = phoneDetails.getString("phone", "");
        token = phoneDetails.getString("token", "");
        location_message_intent = new Intent().setClass(this,LocationMessage.class);
        location_message_intent.putExtra("gid", group_id);
        map_messages = new ArrayList<Marker>();
        loadGroupMessages();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
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
            /*
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
    @Override
    public void onInfoWindowClick(Marker marker){
        int id = (int) marker.getTag();
        // Start Message Feed Activity
        Intent message_feed_intent = new Intent().setClass(this,MessageViewFeed.class);
        message_feed_intent.putExtra("gid", messages.get(id).gid);
        message_feed_intent.putExtra("mid", messages.get(id).mid);
        startActivity(message_feed_intent);
    }
    public void getLocation() {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("MYAPP", "Location Manager Successfully Created");
            }
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                } else {
                    //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                criteria.setAltitudeRequired(true);
                String provider = locationManager.getBestProvider(criteria, false);
                Toast.makeText(this.getApplicationContext(), "Current Provider: " + provider, Toast.LENGTH_SHORT).show();
                //locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(provider);
                    if (location != null) {
                        gps_latitude = location.getLatitude();
                        gps_longitude = location.getLongitude();
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

    public void addMessageToMap(int id, double latitude, double longitude, int message_state, String summary, int gid, int mid, String createdby ) {
        MapMessages messageFeed = new MapMessages();
        messageFeed.latitude = latitude;
        messageFeed.longitude = longitude;
        messageFeed.summary = summary;
        messageFeed.gid = gid;
        messageFeed.mid = mid;
        messageFeed.createdby = createdby;
        messageFeed.message_state = message_state;
        // 0-> unread, 1-> read, 2-> mine
        Marker marker = null;
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
        marker.setSnippet(summary.substring(0,40)+"...");
        map_messages.add(marker);
        messages.add(id,messageFeed);
    }

    public void loadGroupMessages(){
        try{
            JSONObject requestMap = new JSONObject();
            requestMap.put("gid", group_id);
            requestMap.put("phone", phone);
            requestMap.put("token", token);
            JSONObject coordinates=new JSONObject();
            coordinates.put("lat",Double.toString(gps_latitude));
            coordinates.put("long",Double.toString(gps_longitude));
            requestMap.put("location",coordinates);
            Log.d("MYAPP: RequestData",requestMap.toString());
            URLDataHash mydata = new URLDataHash();
            mydata.url = "192.168.43.231";
            mydata.apicall = "user/group/messages";
            mydata.jsonData=requestMap;
            // Request the server
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if(data == null){
                Log.d("MYAPP: ServerResp","Error during server request");
                ArrayList<String> t_name, t_icon, t_unread;
                t_name = new ArrayList<String>();
                t_icon = new ArrayList<String>();
                t_unread = new ArrayList<String>();
                t_name.add("Alpha");
                t_icon.add("null");
                t_unread.add("Unread: 23");
                return;
            }
            JSONArray groups = data.getJSONArray("resp");
            JSONObject currentObj=new JSONObject();
            for(int i=0;i<groups.length();i++)
            {
                currentObj = groups.getJSONObject(i);
                addMessageToMap(i, currentObj.getDouble("lat"), currentObj.getDouble("long"), currentObj.getInt("readStatus"),currentObj.getString("body"), currentObj.getInt("gid"), currentObj.getInt("mid"), currentObj.getString("createdby"));
            }
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
            myMarker=mMap.addMarker(new MarkerOptions().position(myLocation)
                    .title("Mylocation").snippet(myLocation.toString()));
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(myLocation);
            circleOptions.radius(2);
            circleOptions.fillColor(-16711936);
            mMap.addCircle(circleOptions);
            //mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location").snippet("AWESOME").icon(vectorToBitmap(R.drawable.ic_launcher, Color.parseColor("#A4C639"))));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            // Zooming into the map N times.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
            Log.d("MYAPP","Updating Map");
            try {
                GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                if(gpsStatus != null) {
                    Iterable<GpsSatellite>satellites = gpsStatus.getSatellites();
                    Iterator<GpsSatellite> sat = satellites.iterator();
                    String lSatellites = null;
                    int i = 0;
                    Log.i("MYAPP","Showing List");
                    while (sat.hasNext()) {
                        GpsSatellite satellite = sat.next();
                        lSatellites = "Satellite" + (i++) + ": "
                                + satellite.getPrn() + ","
                                + satellite.usedInFix() + ","
                                + satellite.getSnr() + ","
                                + satellite.getAzimuth() + ","
                                + satellite.getElevation()+ "\n\n";

                        Log.d("MYAPP",lSatellites);
                    }
                }
                else{
                    Log.e("MYAPP","Gps Status is NULL");
                }
            }
            catch (SecurityException e){
                Log.d("MYAPP","Security Exception");
            }
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
    public void onResume(){
        super.onResume();
        if (imgButton_location.getVisibility() == View.VISIBLE){
            imgButton_location.setVisibility(View.GONE);
            imgButton_location_tag.setVisibility(View.GONE);
            imgButton_no_location.setVisibility(View.GONE);
            imgButton_no_location_tag.setVisibility(View.GONE);
            tv_message_type.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 301) {
            // Create Message
            if (resultCode == 101) {
                Toast.makeText(getApplicationContext(),"Message Send!",Toast.LENGTH_SHORT).show();
                Log.d("MYAPP",data.getStringExtra("LocationMessageData"));
            }
            else{
                Toast.makeText(getApplicationContext(),"Message Not Send!",Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == 302){

        }
        else if (requestCode == 303){

        }
        else if (requestCode == 304){

        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editgroup:
                // About option clicked.
                Intent intent_group_manager = new Intent().setClass(this,GroupManager.class);
                startActivity(intent_group_manager);
                Toast.makeText(getApplicationContext(),"Group Edit",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_exitgroup:
                // Exit option clicked.
                Toast.makeText(getApplicationContext(),"Group Exit",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_viewread:
                Toast.makeText(getApplicationContext(),"View Read",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_viewunread:
                Toast.makeText(getApplicationContext(),"View Unread",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_searchmap:
                Toast.makeText(getApplicationContext(),"Filter by user",Toast.LENGTH_SHORT).show();
                if(mSearchView.getVisibility() == View.VISIBLE){
                    mSearchView.setVisibility(View.GONE);
                }
                else {
                    mSearchView.setVisibility(View.VISIBLE);
                }
                return true;
            case R.id.action_refresh_map:
                Toast.makeText(getApplicationContext(),"Refresh Done!",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createMessage(View v){
        // Setting Visibility
        if(imgButton_location.getVisibility() == View.GONE){
            imgButton_location.setVisibility(View.VISIBLE);
            imgButton_location_tag.setVisibility(View.VISIBLE);
            imgButton_no_location.setVisibility(View.VISIBLE);
            imgButton_no_location_tag.setVisibility(View.VISIBLE);
            tv_message_type.setVisibility(View.VISIBLE);
        }
        else if (imgButton_location.getVisibility() == View.VISIBLE){
            imgButton_location.setVisibility(View.GONE);
            imgButton_location_tag.setVisibility(View.GONE);
            imgButton_no_location.setVisibility(View.GONE);
            imgButton_no_location_tag.setVisibility(View.GONE);
            tv_message_type.setVisibility(View.GONE);
        }
    }

    public void livevideomode(View v){
        Intent i = new Intent().setClass(this,SensorPage.class);
        startActivity(i);
    }
    public void message_location(View v){
        Log.d("MYAPP","Message Location");
        location_message_intent.putExtra("longitude",gps_longitude);
        location_message_intent.putExtra("latitude", gps_latitude);
        location_message_intent.putExtra("type",1);
        startActivityForResult(location_message_intent,301);
    }
    public void message_location_tag(View v){
        Log.d("MYAPP","Message Location Tag");
        location_message_intent.putExtra("longitude",gps_longitude);
        location_message_intent.putExtra("latitude", gps_latitude);
        location_message_intent.putExtra("type",2);
        startActivityForResult(location_message_intent,302);
    }
    public void message_no_location(View v){
        Log.d("MYAPP","Message No Location");
        location_message_intent.putExtra("longitude",gps_longitude);
        location_message_intent.putExtra("latitude", gps_latitude);
        location_message_intent.putExtra("type",3);
        startActivityForResult(location_message_intent,303);
    }
    public void message_no_location_tag(View v){
        Log.d("MYAPP","Message No Location Tag");
        location_message_intent.putExtra("longitude",gps_longitude);
        location_message_intent.putExtra("latitude", gps_latitude);
        location_message_intent.putExtra("type",4);
        startActivityForResult(location_message_intent,304);
    }

    /**
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}

