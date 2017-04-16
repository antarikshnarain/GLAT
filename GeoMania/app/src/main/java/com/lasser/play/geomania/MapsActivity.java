package com.lasser.play.geomania;

import android.*;
import android.content.Intent;
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
import com.lasser.play.geomania.CustomDataStructure.UserSendMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.zip.Inflater;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener{

    private GoogleMap mMap;
    private Marker myMarker;
    boolean isGPSEnabled = false,isNetworkEnabled = false, canGetLocation = false;
    Location location;
    public double gps_longitude=0.0, gps_latitude=0.0;
    public String provider;
    final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // 10 Meters
    final long MIN_TIME_BW_UPDATES = 1000 * 2 * 1; // 1 minute
    protected LocationManager locationManager;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
        //((GlobalData) this.getApplication()).setGoogleMap(googleMap);
        //Log.d("MYAPP-GoogleMap",((GlobalData) this.getApplication()).getGoogleMap().toString());
        mMap = googleMap;
        /*
        if(gps_latitude>0) {
            LatLng sydney = new LatLng(gpsData.gps_latitude, gpsData.gps_longitude);
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(sydney);
            circleOptions.radius(2);
            circleOptions.fillColor(-16711936);
            googleMap.addCircle(circleOptions);
            googleMap.addMarker(new MarkerOptions().position(sydney)
                    .title("Mylocation").snippet("My first"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            // Zooming into the map N times.
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
        */
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            gps_latitude = location.getLatitude();
            gps_longitude = location.getLongitude();
            updateMap(gps_latitude,gps_longitude);
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
    public void getLocation(){
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(this.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
                Log.d("MYAPP","Location Manager Successfully Created");
            }
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                Criteria criteria = new Criteria();
                provider = locationManager.getBestProvider(criteria, false);
                Toast.makeText(this.getApplicationContext(),"Current Provider: "+provider, Toast.LENGTH_SHORT).show();
                locationManager.requestLocationUpdates(provider, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(provider);
                    if (location != null) {
                        gps_latitude = location.getLatitude();
                        gps_longitude = location.getLongitude();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 101) {
            // Create Message
            if (resultCode == 101) {
                Toast.makeText(getApplicationContext(),"Message Send!",Toast.LENGTH_SHORT).show();
                Log.d("MYAPP",data.getStringExtra("LocationMessageData"));
            }
            else{
                Toast.makeText(getApplicationContext(),"Message Not Send!",Toast.LENGTH_SHORT).show();
            }
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
        /*
        Snackbar snackbar = Snackbar.make(v,"Message Type", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);
        LayoutInflater mInflater = (LayoutInflater) this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
        // Inflate our custom view
        View snackView = mInflater.inflate(R.layout.snackbar_new_message, null);
        // Configure the view
        /*
        ImageView imageView = (ImageView) snackView.findViewById(R.id.image);
        imageView.setImageBitmap(image);
        TextView textViewTop = (TextView) snackView.findViewById(R.id.text);
        textViewTop.setText("abc");
        textViewTop.setTextColor(Color.WHITE);
        */
        // Add the view to the Snackbar's layout
        //layout.addView(snackView,20,20);
        // Show the Snackbar
        //snackbar.show();

    }
    public void message_location(View v){
        Log.d("MYAPP","Message Location");
        Intent intent = new Intent();
        //Bundle b = new Bundle();
        //UserSendMessage message = new UserSendMessage();
        //message.gps_lati = gps_latitude;
        //message.gps_longi = gps_longitude;
        //message.type = 1;
        //b.putSerializable("myobj",new UserSendMessage());
        intent.setClass(this,LocationMessage.class);
        //intent.putExtra("MapsData",b);
        startActivityForResult(intent,101);
    }
    public void message_location_tag(View v){
        Log.d("MYAPP","Message Location Tag");
        Intent intent = new Intent();
        Bundle b = new Bundle();
        UserSendMessage message = new UserSendMessage();
        message.gps_lati = gps_latitude;
        message.gps_longi = gps_longitude;
        message.type = 2;
        b.putSerializable("myobj",new UserSendMessage());
        intent.setClass(this,LocationMessage.class);
        intent.putExtra("MapsData",b);
        startActivityForResult(intent,101);
    }
    public void message_no_location(View v){
        Log.d("MYAPP","Message No Location");
        Intent intent = new Intent();
        Bundle b = new Bundle();
        UserSendMessage message = new UserSendMessage();
        message.gps_lati = gps_latitude;
        message.gps_longi = gps_longitude;
        message.type = 3;
        b.putSerializable("myobj",new UserSendMessage());
        intent.setClass(this,LocationMessage.class);
        intent.putExtra("MapsData",b);
        startActivityForResult(intent,101);
    }
    public void message_no_location_tag(View v){
        Log.d("MYAPP","Message No Location Tag");
        Intent intent = new Intent();
        Bundle b = new Bundle();
        UserSendMessage message = new UserSendMessage();
        message.gps_lati = gps_latitude;
        message.gps_longi = gps_longitude;
        message.type = 4;
        b.putSerializable("myobj",new UserSendMessage());
        intent.setClass(this,LocationMessage.class);
        intent.putExtra("MapsData",b);
        startActivityForResult(intent,101);
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

