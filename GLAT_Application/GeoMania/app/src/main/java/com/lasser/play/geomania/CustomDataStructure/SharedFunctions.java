package com.lasser.play.geomania.CustomDataStructure;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Created by antar on 23-Apr-17.
 */

public class SharedFunctions {

    private Context context;
    //Shared Preferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;
    public String user, token, phone, user_profile_pic;

    // Bitmap Variables
    private final int imgWidth = 240;
    private final int imgHeight = 240;

    // Server Constants
    public final String serverUrl = "52.172.193.163";//"192.168.43.231";
    // External Storage Directory
    public final String root_path;
    // Request Codes
    public final static int GALLERY_IMAGE = 102;
    public final static int IMAGE_CLICK = 401;
    public final static int SUCCESS= 500;
    public final static int FAIL = 900;
    // Constructor
    public SharedFunctions(Context mContext){
        context = mContext;
        root_path = Environment.getExternalStorageDirectory().getPath()+"/GeoMania/";
        String[] localdirs = {"temp","profile_pic","group_icon","object_file","media_file"};
        for (String a: localdirs
             ) {
            File mydir = new File(root_path+a);
            if(!mydir.isDirectory())
                mydir.mkdirs();
        }
        sharedPreferences = context.getSharedPreferences("userdata", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();
        user = sharedPreferences.getString("user","");
        phone = sharedPreferences.getString("phone","");
        token = sharedPreferences.getString("token","");
        user_profile_pic = sharedPreferences.getString("user_profile_pic","");
    }
    // Update Shared Preferences (user, phone, token)
    public void updateSharedPreference(String key, String value){
        sharedPrefEditor.putString(key,value);
        sharedPrefEditor.commit();
        if(key.equals("user"))
            user = value;
        else if(key.equals("phone"))
            phone = value;
        else if(key.equals("token"))
            token = value;
        else if(key.equals("user_profile_pic"))
            user_profile_pic = value;
    }
    // Function to resize Bitmap from File for the Application
    public Bitmap resizeBitmap(String filePath){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = 1;
        if ((imgWidth > 0) || (imgHeight > 0)) {
            scaleFactor = Math.max(photoW/imgWidth, photoH/imgHeight);
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        return BitmapFactory.decodeFile(filePath, bmOptions);
    }
    public void resizeBitmapFile(String filePath, int w, int h){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = 1;
        if ((w > 0) || (h > 0)) {
            scaleFactor = Math.max(photoW/w, photoH/h);
        }
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, bmOptions);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filePath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // Compare Bitmaps
    public boolean bitmapCompare(String file1, String file2) {
        Bitmap bitmap1 = BitmapFactory.decodeFile(file1);
        Bitmap bitmap2 = BitmapFactory.decodeFile(file2);
        ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
        bitmap1.copyPixelsToBuffer(buffer1);

        ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
        bitmap2.copyPixelsToBuffer(buffer2);

        return Arrays.equals(buffer1.array(), buffer2.array());
    }
    // Function get Get Path from Uri
    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    // File to String
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
    // String to File
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
    // Get ProfilePic from folder, missing then request
    public Bitmap setPicture(String filename, int category){
        if(filename.equals("")){
            if(category == 1)
                return BitmapFactory.decodeResource(context.getResources(),R.drawable.profile_icon);
            else if(category == 2)
                return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            else
                return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }
        String imagePath = "";
        if(category == 1)
            imagePath = root_path + "profile_pic/" + filename;
        else if (category == 2)
            imagePath = root_path + "group_icon/" + filename;
        else if (category == 3)
            imagePath = root_path + "media_file/" + filename;
        else
            return null;
        File mFile = new File(imagePath);
        if(mFile.isFile())
            return resizeBitmap(imagePath);
        // File Doesnot Exist Request Server for download
        else {
            if (downloadFile(imagePath, filename))
                return resizeBitmap(imagePath);
        }
        return null;
    }
    // Download file(filename) to file_path
    public boolean downloadFile(String file_path, String filename){
        // Request Server
        try {
            URLDataHash mydata = new URLDataHash();
            mydata.jsonData.put("fileName", filename);
            mydata.jsonData.put("phone", phone);
            mydata.jsonData.put("token", token);
            mydata.url = serverUrl;
            mydata.apicall = "file/download";
            JSONObject data = new nodeHttpRequest(context).execute(mydata).get();
            Toast.makeText(context, "Data Send to Server!", Toast.LENGTH_SHORT).show();
            if (data.getString("status").equals("success")) {
                Toast.makeText(context, "File Downloaded Successfully", Toast.LENGTH_SHORT).show();
                // Saving File after download
                Base64ToFile(data.getString("resp"), new File(file_path));
                return true;
            }
            else{
                Toast.makeText(context, "Failed To Download!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e){ e.printStackTrace(); }
        catch (InterruptedException e){ e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
        return false;
    }
    // Upload File(temp_path) and save it to file_path
    public String uploadFile(String temp_path, String file_path){
        try{
            URLDataHash mydata = new URLDataHash();
            mydata.jsonData.put("file",FileToBase64(new File(temp_path)));
            mydata.jsonData.put("phone",phone);
            mydata.jsonData.put("token",token);
            mydata.url=serverUrl;
            mydata.apicall="file/upload";
            JSONObject data = new nodeHttpRequest(context).execute(mydata).get();
            Toast.makeText(context,"Data Send to Server!",Toast.LENGTH_SHORT).show();
            if (data.getString("status").equals("success")) {
                Toast.makeText(context, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
                // Saving File after download resp="filename"
                file_path += data.getString("resp");
                Base64ToFile(FileToBase64(new File(temp_path)), new File(file_path));
                Log.d("MYAPP: Upload", "File Uploaded "+file_path);
                return data.getString("resp");
            }
            else{
                Toast.makeText(context, "Failed To Upload!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e){ e.printStackTrace(); }
        catch (InterruptedException e){ e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
        return null;
    }
    public void askForPermission(String permission, Integer requestCode){
        if(ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(null,new String[]{permission}, requestCode);
        }
    }
}
