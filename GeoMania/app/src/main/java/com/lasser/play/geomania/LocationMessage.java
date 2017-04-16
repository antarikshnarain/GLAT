package com.lasser.play.geomania;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
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
    int GALLERY_IMAGE = 102;
    int CAPTURE_IMAGE = 105;
    int CROP_IMAGE = 106;
    Uri picUri;

    // View
    LinearLayout media_list;
    CustomGroupListAdapter_MediaList adapter;

    // Custom List adapter
    //String[] itemTitle = {""};
    //String[] itemImage = {""};
    ArrayList<String> itemTitle = new ArrayList<String>();
    ArrayList<String> itemImage = new ArrayList<String>();
    int adapterCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.activity_location_message);
       // message = (UserSendMessage) bundle.getSerializable("MapsData");
        //gps_longitude = bundle.getDouble("gps_longitude",0.0);
        //TYPE = bundle.getInt("type",1);
        /*
        tv_latitude = (TextView) findViewById(R.id.my_latitude);
        tv_longitude = (TextView) findViewById(R.id.my_logitude);
        user_message = (EditText) findViewById(R.id.user_message);
        tv_longitude.setText("Longitude: "+message.gps_longi);
        tv_latitude.setText("Latitude: "+message.gps_lati);
        */
        // Horizontal List View
        adapter = new CustomGroupListAdapter_MediaList(this,itemTitle,itemImage);
        adapterCount = adapter.getCount();
        media_list = (LinearLayout) findViewById(R.id.horizontal_list_view);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == GALLERY_IMAGE){
            // Gallery Images
            if (resultCode == Activity.RESULT_OK){
                Uri selectedImage = data.getData();
                String filePath = getRealPathFromURI(selectedImage);
                Log.d("MYAPP: FilePath",filePath);
                itemTitle.add("Image");
                itemImage.add(filePath);
                media_list.addView(adapter.getView(adapterCount++,null,null));
            }
        }
        else if(requestCode == CAPTURE_IMAGE){
            picUri = data.getData();
            performCrop();
        }
        else if(requestCode == CROP_IMAGE){
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            saveCroppedImage(bitmap);
            ImageView mImage = (ImageView) findViewById(R.id.imageView2);
            mImage.setImageBitmap(bitmap);

        }
    }
    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public void performCrop(){
        try{
            Intent cropImageIntent = new Intent("com.android.camera.action.CROP");
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
    private void saveCroppedImage(Bitmap mBitmap){
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/GeoMania/image123.png");
            mBitmap.compress(Bitmap.CompressFormat.PNG,100, fileOutputStream);
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
        //message.msg.text = user_message.getText().toString();
        //setResult(101,new Intent().putExtra("LocationMessageData",message));
        //finish();
        // Get image from Gallary
        //Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        //photoPickerIntent.setType("image/*");
        //startActivityForResult(photoPickerIntent,102);
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = new File(sdcard.getAbsolutePath() + "/GeoMania/"+"image1.jpg");
        File dir2 = new File(sdcard.getAbsolutePath() + "/GeoMania/"+"image3.jpg");
        //Base64ToImage(ImageToBase64(dir),dir2);
        try{
            sendDataToServer(dir,dir2,"image1.jpg");
        }
        catch(JSONException e){
            e.printStackTrace();
        }
    }
    public void object_generator(View v){
        Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(captureImageIntent,CAPTURE_IMAGE);
        /*
        //mydata.hashMap=hashMap;
        try {
            URLDataHash mydata = new URLDataHash();
            mydata.jsonData.put("INT",2);
            mydata.jsonData.put("FLOAT",0.02);
            mydata.jsonData.put("String","MyString");
            int a[]={1,2,3};
            String b[]={"alpha","beta","gamma"};
            mydata.jsonData.put("Object INT[]",new JSONArray(a));
            JSONObject json1 = new JSONObject();
            json1.put("key1","2222");
            json1.put("key2",new JSONArray(b));
            mydata.jsonData.put("Object JSON[]",json1);
            mydata.url="192.168.43.231";
            mydata.apicall="hello";//"user/signup";
            // Send Request
            Log.d("MYAPP: SEND",mydata.jsonData.toString());
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();

            Log.d("MYAPP: RECV",data.toString());
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        catch (ExecutionException e){
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }*/
    }
    public void addMedia(View v){
        // Getting multiple images from gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Images"),GALLERY_IMAGE);
    }
    public String FileToBase64(File filePath ){
        try {
            Log.d("MYAPP","Starting Image to Base64");
            InputStream inputStream = new FileInputStream(filePath);
            byte[] bytes;
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while((bytesRead = inputStream.read(buffer)) !=-1){
                output.write(buffer,0,bytesRead);
            }
            bytes = output.toByteArray();
            Log.d("MYAPP","Starting Image to Base64 Sending Data");
            return Base64.encodeToString(bytes,Base64.DEFAULT);
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return "";
    }
    public void Base64ToFile(String data, File filename){
        try {
            Log.d("MYAPP","Starting Base64 to File");
            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            FileOutputStream out = new FileOutputStream(filename);
            out.write(bytes);
            Log.d("MYAPP","File Written");
            out.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public void sendDataToServer(File dir1, File dir2,String filename) throws JSONException{
        URLDataHash mydata = new URLDataHash();
        mydata.jsonData.put("image",FileToBase64(dir1));
        mydata.jsonData.put("fileName",filename);
        mydata.url="192.168.43.231";
        mydata.apicall="imageTest";//"user/signup";
        //mydata.hashMap=hashMap;
        try {
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            Toast.makeText(getApplicationContext(),"Data Send to Server!",Toast.LENGTH_SHORT).show();
            String fileData = data.getString("image");
            String filename_new = data.getString("fileName");
            Base64ToFile(fileData,dir2);
        }
        catch (JSONException e){
            e.printStackTrace();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
