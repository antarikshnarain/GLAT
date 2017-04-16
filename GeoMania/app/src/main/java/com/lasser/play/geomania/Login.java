package com.lasser.play.geomania;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
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

import static com.lasser.play.geomania.GroupView.MyPREFERENCES;

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
        //*
        //Intent i = new Intent().setClass(this,MapsActivity.class);
        //startActivity(i);
        //
        setContentView(R.layout.activity_login);
        loginFlag = false;
        username = (EditText) findViewById(R.id.username);
        userphoneno = (EditText) findViewById(R.id.phoneno);
        userotp = (EditText) findViewById(R.id.userotp);
        sharedPref = getSharedPreferences("userdata",Context.MODE_PRIVATE);
        String auto_name = sharedPref.getString("name", "");
        String auto_phone = sharedPref.getString("phone", "");
        String auto_token = sharedPref.getString("token", "");
        intent_group_view = new Intent().setClass(this,MapsActivity.class);
        if(!auto_name.equals("") && !auto_phone.equals("") && !auto_token.equals("")){
            Toast.makeText(this,"Auto Login Successful!",Toast.LENGTH_SHORT).show();
            startActivity(intent_group_view);
        }
        //TelephonyManager telephonyManager = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        //String mPhoneNo = telephonyManager.getSimSerialNumber();
    }
    public void updateList(final String smsBody, final String smsAddress) {
        //Toast.makeText(getApplicationContext(),"HEY",Toast.LENGTH_LONG).show();
        Log.d("MYAPP","ReceivedPro:"+smsAddress+"\n"+smsBody);
        myOTP = smsBody;
        userotp.setText(myOTP);
        //tv.setText("ReceivedPro:"+smsAddress+"\n"+smsBody);
    }
    public void checkLogin(View v) throws JSONException{
        //startActivity(intent_group_view);
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
                JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
                Toast.makeText(getApplicationContext(),"Data Send to Server!",Toast.LENGTH_SHORT).show();
                try {
                    if (data.getString("status").equals("success")) {
                        serverOTP = data.getJSONObject("resp").getString("otp");
                        Toast.makeText(this,"Server OTP: " + serverOTP, Toast.LENGTH_SHORT).show();
                        /*
                        if (CheckOtp(myOTP)) {
                            mydata.apicall="user/otpverify";
                            json.remove("name");
                            JSONObject tokenData = new nodeHttpRequest(this).execute(mydata).get();
                            if (tokenData.getString("status").equals("success")) {
                                String mtoken = tokenData.getJSONObject("resp").getString("token");
                                Toast.makeText(this,"TOKEN:"+ mtoken,Toast.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("username", mname);
                                editor.putString("phoneno", mphone);
                                editor.putString("token", mtoken);
                                editor.commit();
                                startActivity(intent_group_view);
                            }
                            else{
                                Toast.makeText(this,"Didn't Receive Access Token from Server!",Toast.LENGTH_SHORT).show();
                            }
                        }*/
                    }
                    else {
                        Toast.makeText(this,"Error in Login Credentials",Toast.LENGTH_SHORT).show();
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
        else{
            Toast.makeText(this,"Error Login, Invaild OTP!",Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    public void otpverify(View v) throws JSONException{
        myOTP = userotp.getText().toString();
        // Override
        if( myOTP.equals("0000")){

        }
        if (CheckOtp(myOTP)) {
            try {
                JSONObject json = new JSONObject();
                json.put("phone", userphoneno.getText().toString());
                URLDataHash mydata = new URLDataHash();
                mydata.url = "192.168.43.231";
                mydata.apicall = "user/login";//"imageTest";
                mydata.jsonData = json;
                JSONObject tokenData = new nodeHttpRequest(this).execute(mydata).get();
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
                } else {
                    Toast.makeText(this, "Didn't Receive Access Token from Server!", Toast.LENGTH_SHORT).show();
                }
            }
            catch(InterruptedException e){
                e.printStackTrace();
            }
            catch(ExecutionException e){
                e.printStackTrace();
            }
        }
        /*
        EditText uotp = (EditText) findViewById(R.id.userotp);
        JSONObject json = new JSONObject();
        json.put("phone",userphoneno.getText().toString());
        URLDataHash mydata = new URLDataHash();
        mydata.url="192.168.43.231";
        mydata.apicall="user/signup";
        //mydata.hashMap=hashMap;
        if(CheckOtp(uotp.getText().toString())) {
            mydata.apicall = "user/otpverify";
            try {
                JSONObject tokenData = new nodeHttpRequest(this).execute(mydata).get();
                try{
                if (tokenData.getString("status").equals("success")) {
                    //tv.setText(tokenData.getJSONObject("resp").getString("token"));
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
        }*/
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
