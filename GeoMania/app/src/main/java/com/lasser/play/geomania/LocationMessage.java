package com.lasser.play.geomania;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.SensorData;
import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;
import com.lasser.play.geomania.CustomDataStructure.UserSendMessage;
import com.lasser.play.geomania.ListAdapter.CustomGroupListAdapter_MediaList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class LocationMessage extends AppCompatActivity {

    TextView tv_latitude, tv_longitude;
    EditText user_message;
    double gps_longitude, gps_latitude;
    int TYPE;
    UserSendMessage message;
    String imageData;
    String object_file = "";
    int GALLERY_IMAGE = 102;
    int CAPTURE_IMAGE = 105;
    int CROP_IMAGE = 106;
    Uri picUri;

    SharedFunctions myfunction;
    SensorData sensorData;
    String phone, token, group_id;
    int type;
    // View
    LinearLayout media_list;
    CustomGroupListAdapter_MediaList adapter;
    ArrayList<String> itemTitle = new ArrayList<String>();
    ArrayList<String> itemImage = new ArrayList<String>();
    int adapterCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_message);
        // Getting Data from MapsActivity
        Intent data = getIntent();
        gps_latitude = data.getDoubleExtra("latitude",0.0);
        gps_longitude = data.getDoubleExtra("longitude",0.0);
        group_id = data.getStringExtra("gid");
        type = data.getIntExtra("type",0);
        myfunction = new SharedFunctions(this);
        sensorData = new SensorData(this);
        // Horizontal List View
        adapter = new CustomGroupListAdapter_MediaList(this,itemTitle,itemImage);
        adapterCount = adapter.getCount();
        user_message = (EditText) findViewById(R.id.user_message);
        media_list = (LinearLayout) findViewById(R.id.horizontal_list_view);
        tv_latitude = (TextView) findViewById(R.id.my_latitude);
        tv_longitude = (TextView) findViewById(R.id.my_longitude);
        tv_latitude.setText("Latitude: "+gps_latitude);
        tv_longitude.setText("Longitude: "+gps_longitude);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == myfunction.GALLERY_IMAGE){
            // Gallery Images
            if (resultCode == Activity.RESULT_OK){
                Uri selectedImage = data.getData();
                String filePath = myfunction.getRealPathFromURI(selectedImage);
                Log.d("MYAPP: FilePath",filePath);
                itemTitle.add("Image");
                itemImage.add(filePath);
                media_list.addView(adapter.getView(adapterCount++,null,null));
            }
        }
        else if(requestCode == myfunction.IMAGE_CLICK){
            if(resultCode == myfunction.SUCCESS)
                performCrop(new File(myfunction.root_path+"temp/"+"cameraPic.jpg"));
        }
        else if(requestCode == CROP_IMAGE){
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            saveCroppedImage(bitmap, "myobject.jpg");
            ImageView mImage = (ImageView) findViewById(R.id.imageView2);
            mImage.setImageBitmap(bitmap);

        }
    }
    public void performCrop(File mFile){
        try{
            Intent cropImageIntent = new Intent("com.android.camera.action.CROP");
            picUri = Uri.fromFile(mFile);
            cropImageIntent.setDataAndType(picUri, "image/*");
            cropImageIntent.putExtra("crop","true");
            cropImageIntent.putExtra("aspectX",1);
            cropImageIntent.putExtra("aspectY",1);
            cropImageIntent.putExtra("outputX",1024);
            cropImageIntent.putExtra("outputY",1024);
            //cropImageIntent.putExtra("scale",false);
            cropImageIntent.putExtra("return-data",true);
            startActivityForResult(cropImageIntent,CROP_IMAGE);
        }
        catch (ActivityNotFoundException e){
            e.printStackTrace();
        }
    }
    private void saveCroppedImage(Bitmap mBitmap, String filename){
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(myfunction.root_path+"object_file/"+filename);
            mBitmap.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream);
            object_file = myfunction.uploadFile(myfunction.root_path+"object_file/"+filename, myfunction.root_path+"object_file/");
            Toast.makeText(getApplicationContext(),"Image Saved Successfully",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try{
                if(fileOutputStream != null){
                    fileOutputStream.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public void send_geo_message(View v){
        String userMessage = user_message.getText().toString();
        if(userMessage.equals(""))
            return;
        try{
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "group/message/add";
            mydata.jsonData.put("phone",myfunction.phone);
            mydata.jsonData.put("token",myfunction.token);
            mydata.jsonData.put("lat",gps_latitude);
            mydata.jsonData.put("long",gps_longitude);
            mydata.jsonData.put("gid",group_id);
            ArrayList<String> mediaFile = new ArrayList<String>();
            for (String item: itemImage) {
                mediaFile.add(myfunction.uploadFile(item, myfunction.root_path+ "media_file/"));
            }
            JSONObject sensor_data = new JSONObject();
            sensor_data.put("sensor",sensorData.sensorDataJson());
            sensor_data.put("media",mediaFile);
            sensor_data.put("object", object_file);
            mydata.jsonData.put("sensorData", sensor_data);
            mydata.jsonData.put("body", userMessage);
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if (data == null) {
                Log.d("MYAPP: ServerResp", "Error during server request");
                return;
            }
            if (data.getString("status").equals("success"))
                Toast.makeText(this, "Message Created Successfully", Toast.LENGTH_SHORT).show();
        }
        catch(JSONException e){ e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
    }
    public void object_generator(View v){
        // Custom Camera App
        Intent intent_mycamera = new Intent().setClass(this,MyCamera.class);
        startActivityForResult(intent_mycamera, myfunction.IMAGE_CLICK);
        /*
        // System Camera App
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(captureImageIntent,CAPTURE_IMAGE);
        */
    }
    public void addMedia(View v){
        // Getting multiple images from gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Images"),GALLERY_IMAGE);
    }
}
