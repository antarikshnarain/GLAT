package com.lasser.play.geomania;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.json.JSONObject.NULL;

public class GroupView extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs" ;
    LocationManager locationManager;
    double longitudeGPS, latitudeGPS;
    TextView testDisplay;
    ListView listView;
    SearchView mSearchView;
    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }


    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_view);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        testDisplay= (TextView) findViewById(R.id.textView);
        mSearchView = (SearchView) findViewById(R.id.groupsearchview);
        /*
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 2 * 60 * 1000, 10, locationListenerNetwork);


            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            testDisplay.setText(longitudeGPS + "");
            //  testDisplay.setText(latitudeGPS + "");
        */
        SharedPreferences phoneDetails = getSharedPreferences("userdata", MODE_PRIVATE);

        String PhoneValue = phoneDetails.getString("P    public static final String MyPREFERENCES = \"MyPrefs\" ;\nhone", "");
        String TokenValue = phoneDetails.getString("Token", "");

        JSONObject requestMap = new JSONObject();
        try {
            requestMap.put("phone", PhoneValue);
            requestMap.put("token", TokenValue);
            requestMap.put("mode", "create");

            JSONObject coordinates=new JSONObject();
            //coordinates = Double.toString(latitudeGPS) + "," + Double.toString(longitudeGPS);

            coordinates.put("latitude",Double.toString(latitudeGPS));
            coordinates.put("longitude",Double.toString(longitudeGPS));

            requestMap.put("location",coordinates);

        }
        catch(JSONException e){}



        testDisplay.setText(Double.toString(longitudeGPS)+ " "+ Double.toString(latitudeGPS));


        Log.d("Coordinates",Double.toString(longitudeGPS)+ " "+ Double.toString(latitudeGPS));
        Log.d("My OUTPUT", PhoneValue + " " + TokenValue);

        URLDataHash mydata = new URLDataHash();
        mydata.url = "192.168.43.231";
        mydata.apicall = "user/group/list";
        mydata.jsonData=requestMap;

        /*
        try {

            final JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            Log.d("MYAPP:", data.toString());


            String response = data.toString();

            if(response==NULL)
            {
                Log.d("NULL object","");

            }
            else {
                try{
                    JSONArray groups = data.getJSONArray("resp");
                }catch(JSONException e){}


                JSONObject currentObj=new JSONObject();
                final JSONObject GroupIdNameHash=new JSONObject();
                final ArrayList<String> groupList=new ArrayList<String>();
                for(int i=0;i<groups.length();i++)
                {
                    try {
                        currentObj = groups.getJSONObject(i);
                        Log.d("MYAPP: Json Parse", currentObj.toString());
                        groupList.add(currentObj.getString("gname"));
                        GroupIdNameHash.put(currentObj.getString("gname"), currentObj.getString("gid"));
                    }
                    catch(JSONException e){}


                    Log.d("MYAPP: Group Names",groupList.toString());
                }


                listView = (ListView) findViewById(R.id.showGroups);

                final ArrayAdapter adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1,
                        groupList);


                listView.setAdapter(adapter);

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addGroupButton);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
             Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();




//                        Intent Intent = new Intent(getApplicationContext(), DisplayContacts.class);
 //                       view.getContext().startActivity(Intent);
                    }
                });


            }

        }
        catch (InterruptedException e){
            e.printStackTrace();

        }
        catch (ExecutionException e){
            e.printStackTrace();

        }

        */

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_editprofile:
                // About option clicked.
                //Toast.makeText(getApplicationContext(),"Group Edit",Toast.LENGTH_SHORT).show();
                Intent intent_edit_profile = new Intent().setClass(this, EditProfile.class);
                startActivity(intent_edit_profile);
                return true;
            case R.id.action_refresh_group:
                Toast.makeText(getApplicationContext(), "Refresh Group List", Toast.LENGTH_SHORT).show();
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
    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitudeGPS = location.getLongitude();
            latitudeGPS = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    testDisplay.setText(longitudeGPS + "");
                    testDisplay.setText(latitudeGPS + "");
                    Toast.makeText(getApplicationContext(), "Network Provider update", Toast.LENGTH_SHORT).show();
                }
            });
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



}
