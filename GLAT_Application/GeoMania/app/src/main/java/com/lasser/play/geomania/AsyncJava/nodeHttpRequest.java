package com.lasser.play.geomania.AsyncJava;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.lasser.play.geomania.CustomDataStructure.URLDataHash;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

/**
 * Created by antar on 05-Feb-17.
 */


public class nodeHttpRequest extends AsyncTask<URLDataHash, Void, JSONObject> {

    private Context context;
    public String requestData;
    public nodeHttpRequest(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {

    }

    @Override
    protected JSONObject doInBackground(URLDataHash... mydata) {
        //"192.168.43.231"
        /*
        Organizing data from mydata
         */
        String link="http://"+mydata[0].url+":8080"+"/"+mydata[0].apicall;
        // Sending data over HTTP
        BufferedReader bufferedReader;
        String result;
        try {
            String message = mydata[0].jsonData.toString();
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(10000);
            con.setConnectTimeout(15000);
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setChunkedStreamingMode(0);// Size of data unknown
            // making http header
            con.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            con.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            //Connect
            con.connect();
            //setup send
            OutputStream os = new BufferedOutputStream(con.getOutputStream());
            os.write(message.getBytes());
            os.flush();
            bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            result = bufferedReader.readLine();
            //con.disconnect();
            // Parsing String to JSON
            // note: all data received is in text format,
            Log.d("MYAPP",result);
            JSONObject myobj = new JSONObject(result);
            return myobj;

        } catch (Exception e) {
            //Toast.makeText(context,"Server Not Found",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }

    }

    private String getStringFromBitmap(Bitmap bitmap){
        // Image to String
        final int COMPRESSION_QUALITY = 100;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, baos);
        byte[] mBytes = baos.toByteArray();
        return Base64.encodeToString(mBytes,Base64.DEFAULT);
    }
    private Bitmap getBitmapFromString(String imageString){
        // String to Image
        byte[] decodeString = Base64.decode(imageString,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodeString,0,decodeString.length);
    }
    private String getStringFromFile(String path){
        final StringBuilder stringBuilder = new StringBuilder();
        try{
            File file = new File(path);
            final InputStream inputStream = new FileInputStream(file);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while(line != null){
                stringBuilder.append(line+'\n');
                line = reader.readLine();
            }
            reader.close();
            inputStream.close();
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        Log.d("MYAPP: File Data",stringBuilder.toString());
        return stringBuilder.toString();
    }
    @Override
    protected void onPostExecute(JSONObject result) {
        super.onPostExecute(result);
        //this.requestData = result;
        //Log.d("Data",result);
    }
}
