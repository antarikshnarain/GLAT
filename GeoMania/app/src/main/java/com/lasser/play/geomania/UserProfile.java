package com.lasser.play.geomania;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class UserProfile extends Activity{

    private static UserProfile inst;
    TextView tv;
    EditText username,userphoneno;
    SharedPreferences sharedPref;
    String myOTP = "";
    public static UserProfile instance() {
        return inst;
    }
    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        username = (EditText) findViewById(R.id.username);
        userphoneno = (EditText) findViewById(R.id.phoneno);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        String mname = sharedPref.getString("username", "");
        String mphone = sharedPref.getString("password", "");

        // Example of a call to a native method
        tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        //Intent intent = new Intent();
        //intent.setClass(this,LoginActivity.class);
        //startActivity(intent);
        TelephonyManager telephonyManager = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNo = telephonyManager.getSimSerialNumber();
        tv.setText(mPhoneNo);
    }
    public void updateList(final String smsBody, final String smsAddress) {
        //Toast.makeText(getApplicationContext(),"HEY",Toast.LENGTH_LONG).show();
        Log.d("MYAPP","ReceivedPro:"+smsAddress+"\n"+smsBody);
        tv.setText("ReceivedPro:"+smsAddress+"\n"+smsBody);
    }
    public void checkLogin(View v){
        String mname = username.getText().toString();
        String mphone = userphoneno.getText().toString();
        if (mname != "" && mphone != ""){
            HashMap<String,String> hashMap = new HashMap<String,String>();
            hashMap.put("name",mname);
            hashMap.put("phone",mphone);
            URLDataHash mydata = new URLDataHash();
            mydata.url="192.168.43.231";
            mydata.apicall="user/signup";
            mydata.hashMap=hashMap;
            try {
                JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
                Log.d("MYAPP",data.toString());
                try {
                    tv.setText(data.getString("status"));
                    if (data.getString("status").equals("success")) {
                        tv.setText(data.getJSONObject("resp").getString("otp"));
                        myOTP = data.getJSONObject("resp").getString("otp");
                        /*
                        if (CheckOtp(data.getJSONObject("resp").getString("otp"))) {
                            mydata.apicall="user/otpverify";
                            hashMap.remove("name");
                            JSONObject tokenData = new nodeHttpRequest(this).execute(mydata).get();
                            if (tokenData.getString("status").equals("success")) {
                                tv.setText(tokenData.getJSONObject("resp").getString("token"));
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("username", mname);
                                editor.putString("phoneno", mphone);
                                editor.putString("token", tokenData.getJSONObject("resp").getString("token"));
                                editor.commit();
                                Intent intent = new Intent();
                                intent.setclass(this,ProfilePage.class);
                                startActivity(intent);
                            }
                        }*/
                    }
                    else {
                        tv.setText(data.getString("err"));
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
            catch (InterruptedException e){

            }
            catch (ExecutionException e){

            }
        }
        // long highScore = sharedPref.getInt(getString(R.string.saved_high_score), defaultValue);
    }
    public boolean CheckOtp(String serverOtp){

        if (serverOtp.equals(myOTP)){
            return true;
        }
        else{
            tv.setText("Error Login, Invaild OTP!" + sharedPref.getString("OTP",""));
        }
        return false;
    }
    public void otpverify(View v){
        EditText uotp = (EditText) findViewById(R.id.otp);
        HashMap<String,String> hashMap = new HashMap<String,String>();
        hashMap.put("phone",userphoneno.getText().toString());
        URLDataHash mydata = new URLDataHash();
        mydata.url="192.168.43.231";
        mydata.apicall="user/signup";
        mydata.hashMap=hashMap;
        if(CheckOtp(uotp.getText().toString())) {
            mydata.apicall = "user/otpverify";
            try {
                JSONObject tokenData = new nodeHttpRequest(this).execute(mydata).get();
                try{
                if (tokenData.getString("status").equals("success")) {
                    tv.setText(tokenData.getJSONObject("resp").getString("token"));
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("username", username.getText().toString());
                    editor.putString("phoneno", userphoneno.getText().toString());
                    editor.putString("token", tokenData.getJSONObject("resp").getString("token"));
                    editor.commit();
                }}
                catch(JSONException e){}
            }
            catch (InterruptedException e){}
            catch (ExecutionException e){}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
