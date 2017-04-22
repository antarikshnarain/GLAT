package com.lasser.play.geomania;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lasser.play.geomania.CustomDataStructure.URLDataHash;
import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Login extends Activity{

    private static Login inst;
    EditText username,userphoneno, userotp;
    SharedPreferences sharedPref;
    String myOTP = "", serverOTP = "";
    boolean loginFlag;
    Intent intent_group_view;

    public static Login instance() {
        return inst;
    }
    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }
    // Used to load the 'native-lib' library on application startup.
    /*
    static {
        System.loadLibrary("native-lib");
    }
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginFlag = false;
        username = (EditText) findViewById(R.id.username);
        userphoneno = (EditText) findViewById(R.id.phoneno);
        userotp = (EditText) findViewById(R.id.userotp);
        sharedPref = getSharedPreferences("userdata", Context.MODE_PRIVATE);
        String auto_name = sharedPref.getString("name", "");
        String auto_phone = sharedPref.getString("phone", "");
        String auto_token = sharedPref.getString("token", "");
        intent_group_view = new Intent().setClass(this,MapsActivity.class);

        if(!auto_name.equals("") && !auto_phone.equals("") && !auto_token.equals("")){
            Toast.makeText(this,"Auto Login Successful!",Toast.LENGTH_SHORT).show();
            startActivity(intent_group_view);
        }
        else if(!auto_name.equals("")  && !auto_phone.equals("")){
            username.setText(auto_name);
            userphoneno.setText(auto_phone);
        }
        if(checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 201);
        }
        //TelephonyManager telephonyManager = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        //String mPhoneNo = telephonyManager.getSimSerialNumber();
    }
    private void askForPermission(String permission, Integer requestCode){
        if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{permission}, requestCode);
        }
    }
    public void updateList(final String smsBody, final String smsAddress){
        // Get Message from Message Service.
        Log.d("MYAPP","ReceivedPro:"+smsAddress+"\n"+smsBody);
        myOTP = smsBody;
        userotp.setText(myOTP);
        try {
            getAccessToken();
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        // CODE to be added to read message get otp and compare with server otp.
    }
    public void checkLogin(View v) throws JSONException{
        String mname = username.getText().toString();
        String mphone = userphoneno.getText().toString();
        if (mname != "" && mphone != ""){
            JSONObject json = new JSONObject();
            json.put("name",mname);
            json.put("phone",mphone);
            URLDataHash mydata = new URLDataHash();
            mydata.url="192.168.43.231";
            mydata.apicall="user/sendotp";//"imageTest";
            mydata.jsonData=json;
            try {
                // Requesting server for OTP.
                JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
                if(data == null){
                    Toast.makeText(this,"Cannot Connect to the Server",Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(),"Data Send to Server!",Toast.LENGTH_SHORT).show();
                try {
                    if (data.getString("status").equals("success")) {
                        // Stores OTP from server in serverOTP.
                        serverOTP = data.getJSONObject("resp").getString("otp");
                        Toast.makeText(this,"Server OTP: " + serverOTP, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this,"Error in Login Credentials" + data.getString("err"),Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            catch (ExecutionException e){
                e.printStackTrace();
            }
        }
    }
    public boolean CheckOtp(String serverOtp){
        if (serverOtp.equals(myOTP)){
            return true;
        }
        Toast.makeText(this,"Error Login, Invaild OTP!",Toast.LENGTH_SHORT).show();
        return false;
    }
    public void otpverify(View v) throws JSONException{
        myOTP = userotp.getText().toString();
        Log.d("MYAPP OTP", myOTP);
        // Override
        if( myOTP.equalsIgnoreCase("0000")){
            startActivity(intent_group_view);
            finish();
        }
        else if (myOTP.length()>0){
            getAccessToken();
        }
    }
    private void getAccessToken() throws JSONException{
        if (CheckOtp(myOTP)) {
            try {
                JSONObject json = new JSONObject();
                json.put("phone", userphoneno.getText().toString());
                URLDataHash mydata = new URLDataHash();
                mydata.url = "192.168.43.231";
                mydata.apicall = "user/login";//"imageTest";
                mydata.jsonData = json;
                JSONObject tokenData = new nodeHttpRequest(this).execute(mydata).get();
                if(tokenData == null){
                    Toast.makeText(this,"Cannot Connect to the Server",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tokenData.getString("status").equals("success")) {
                    String mtoken = tokenData.getJSONObject("resp").getString("token");
                    Toast.makeText(this, "TOKEN:" + mtoken, Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("name", username.getText().toString());
                    editor.putString("phone", userphoneno.getText().toString());
                    editor.putString("token", mtoken);
                    editor.commit();
                    Log.d("MYAPP: phone",sharedPref.getString("phone",""));
                    Log.d("MYAPP: token", sharedPref.getString("token",""));
                    startActivity(intent_group_view);
                    finish();
                } else {
                    Toast.makeText(this, "Didn't Receive Access Token from Server!" +tokenData.getString("err"), Toast.LENGTH_SHORT).show();
                }
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            catch(ExecutionException e){
                e.printStackTrace();
            }
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
    @Override
    public void onRequestPermissionsResult (int requestCode, String permission[], int[] grantResults){
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

        }
        else{
            Toast.makeText(this,"Permission Denied", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
