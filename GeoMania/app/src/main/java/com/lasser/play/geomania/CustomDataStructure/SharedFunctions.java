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
import java.util.concurrent.ExecutionException;

/**
 * Created by antar on 23-Apr-17.
 */

public class SharedFunctions {

    private Context context;
    //Shared Preferences
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPrefEditor;
    public String user, token, phone;

    // Bitmap Variables
    private final int imgWidth = 60;
    private final int imgHeight = 60;

    // Server Constants
    public final String serverUrl = "192.168.43.231";
    // External Storage Directory
    public final String root_path;
    // Request Codes
    public final static int GALLERY_IMAGE = 102;

    // Constructor
    public SharedFunctions(Context mContext){
        context = mContext;
        root_path = Environment.getExternalStorageDirectory().getPath()+"/GeoMania/";
        String[] localdirs = {"temp","profile_pic","group_icon","object_file"};
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
        String imagePath = "";
        if(category == 1)
            imagePath = root_path + "ProfilePics/" + filename;
        else if (category == 2)
            imagePath = root_path + "GroupIcon/" + filename;
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
    public boolean uploadFile(String temp_path, String file_path){
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
                // Saving File after download
                file_path += data.getString("fileName");
                Base64ToFile(data.getString("resp"), new File(file_path));
                return true;
            }
            else{
                Toast.makeText(context, "Failed To Upload!", Toast.LENGTH_SHORT).show();
            }
        }
        catch (JSONException e){ e.printStackTrace(); }
        catch (InterruptedException e){ e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
        return false;
    }
    public void askForPermission(String permission, Integer requestCode){
        if(ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(null,new String[]{permission}, requestCode);
        }
    }
}
