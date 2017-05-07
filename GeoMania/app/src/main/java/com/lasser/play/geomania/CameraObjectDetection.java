package com.lasser.play.geomania;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class CameraObjectDetection extends AppCompatActivity {

    SharedFunctions myfunction;
    boolean flag_found;
    String object_file_name;
    String object_file;
    Uri picUri;
    ImageView detectedObject;
    int CROP_IMAGE = 106;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_object_detection);
        detectedObject = (ImageView) findViewById(R.id.imageView_detectedObject);
        flag_found = false;
        Intent intent = getIntent();
        object_file_name = intent.getStringExtra("object");
        myfunction = new SharedFunctions(this);
        Intent camera_intent = new Intent().setClass(this,MyCamera.class);
        startActivityForResult(camera_intent, myfunction.IMAGE_CLICK);
    }
    public void sendImageToServer(String filePath){
        // Send Scene file to server
        myfunction.resizeBitmapFile(myfunction.root_path+"temp/"+"cameraPic.jpg",768,1024);
        String scene_file_name = myfunction.uploadFile(filePath,myfunction.root_path+"object_file/");
        Log.d("MYAPP","Done Uploading Scene File");
        try {
            Log.d("MYAPP","Sending Data for Object Detection");
            JSONObject requestMap = new JSONObject();
            requestMap.put("phone", myfunction.phone);
            requestMap.put("token", myfunction.token);
            requestMap.put("objectPath", object_file_name);
            requestMap.put("sceneImg",scene_file_name);
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "group/message/detect";
            mydata.jsonData = requestMap;
            // Request the server
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if (data == null) {
                Log.d("MYAPP: ServerResp", "Error during server request");
                return;
            }
            if(data.getString("status").equals("success")){
                detectedObject.setImageBitmap(myfunction.setPicture(data.getString("resp"),3));

                if(myfunction.bitmapCompare(myfunction.root_path + "media_file/"+data.getString("resp"),filePath)) {
                    flag_found = false;
                    Log.d("MYAPP: bitmap_comp","True");
                    Toast.makeText(getApplicationContext(), "Object Not Found", Toast.LENGTH_SHORT).show();
                }
                else {
                    flag_found = true;
                    Log.d("MYAPP: bitmap_comp","False");
                    Toast.makeText(getApplicationContext(), "Object Found", Toast.LENGTH_SHORT).show();
                }
            }

        }
        catch (JSONException e) { e.printStackTrace(); }
        catch (InterruptedException e){ e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if(requestCode == myfunction.IMAGE_CLICK) {
            if (resultCode == myfunction.SUCCESS) {
                Log.d("MYAPP","Message Captured Sendingto server");
                Toast.makeText(getApplicationContext(), "Sending Scene Image!", Toast.LENGTH_SHORT).show();
                //performCrop(new File(myfunction.root_path+"temp/"+"cameraPic.jpg"));
                sendImageToServer(myfunction.root_path+"temp/"+"cameraPic.jpg");
            } else {
                Toast.makeText(getApplicationContext(), "Image not Clicked!", Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == CROP_IMAGE){
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            saveCroppedImage(bitmap, "scene.jpg");
            detectedObject.setImageBitmap(bitmap);
            sendImageToServer(myfunction.root_path+"object_file/"+"scene.jpg");
        }
    }
    public void exitActivity(View v){
        if(flag_found)
            setResult(myfunction.SUCCESS);
        else
            setResult(myfunction.FAIL);
        finish();
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
            //Toast.makeText(getApplicationContext(),"Image Saved Successfully",Toast.LENGTH_SHORT).show();
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
}
