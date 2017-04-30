package com.lasser.play.geomania;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.CustomDataStructure.URLDataHash;
import com.lasser.play.geomania.ListAdapter.CustomGroupListAdapter_MessageFeed;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MessageViewFeed extends AppCompatActivity {

    ListView listView_feed;
    EditText editText_my_message;
    int group_id, message_id;

    SharedFunctions myfunction;
    CustomGroupListAdapter_MessageFeed adapter;
    ArrayList<String> user;
    ArrayList<String> message;
    ArrayList<String> usericon;
    ArrayList<String> timestamp;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view_feed);
        Intent intent = getIntent();
        group_id = intent.getIntExtra("gid",0);
        message_id = intent.getIntExtra("mid",0);
        listView_feed = (ListView) findViewById(R.id.listview_message_feed);
        editText_my_message = (EditText) findViewById(R.id.editText_my_message);

        myfunction = new SharedFunctions(this);
        user = new ArrayList<>(); message = new ArrayList<>(); usericon = new ArrayList<>(); timestamp = new ArrayList<>();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(":((())):");
        progressDialog.setMessage("Retrieving Messages ...");
        progressDialog.show();
        getMessages();
    }

    public void getMessages(){
        try{
            user = new ArrayList<>(); message = new ArrayList<>(); usericon = new ArrayList<>(); timestamp = new ArrayList<>();
            JSONObject json = new JSONObject();
            json.put("phone",myfunction.phone);
            json.put("token",myfunction.token);
            json.put("gid",group_id);
            json.put("mid",message_id);
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "group/displayMessageFeed";
            mydata.jsonData = json;
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if(data == null){
                Log.d("MYAPP: ServerResp", "Error during Server Request");
                return;
            }
            JSONArray feed_messages = data.getJSONArray("resp");
            JSONObject currobject = new JSONObject();
            for (int i=0; i<feed_messages.length(); i++){
                currobject = feed_messages.getJSONObject(i);
                user.add(i,currobject.getString("user"));
                message.add(i,currobject.getString("message"));
                timestamp.add(i,currobject.getString("createdAt"));
                usericon.add(i,currobject.getString("pic_location"));
            }
            Log.d("MYAPP: Feed", "Length "+user.size());
            adapter = new CustomGroupListAdapter_MessageFeed(this, user, message, timestamp, usericon);
            listView_feed.setAdapter(adapter);
            //adapter.notifyDataSetChanged();
            Log.d("MYAPP: Feed", "Length Adapter"+adapter.getCount());
            progressDialog.dismiss();
        }
        catch (JSONException e ){ e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
    }

    public void addMessageToFeed(View v){
        String my_message = editText_my_message.getText().toString();
        editText_my_message.setText("");
        if (my_message.equals("")) {
            getMessages();
            return;
        }
        // Update Message to server and refresh
        try {
            URLDataHash mydata = new URLDataHash();
            mydata.url = myfunction.serverUrl;
            mydata.apicall = "group/addMessageFeed";
            mydata.jsonData.put("phone", myfunction.phone);
            mydata.jsonData.put("token", myfunction.token);
            mydata.jsonData.put("gid", group_id);
            mydata.jsonData.put("mid", message_id);
            mydata.jsonData.put("comment", my_message);
            JSONObject data = new nodeHttpRequest(this).execute(mydata).get();
            if (data == null) {
                Log.d("MYAPP: ServerResp", "Error during server request");
                return;
            }
            if (data.getString("status").equals("success"))
                Toast.makeText(this, "Message Send!", Toast.LENGTH_SHORT).show();
        }
        catch (JSONException e) { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
        getMessages();
        // Requesting server for data
        /*
        user.add(myfunction.user);
        message.add(my_message);
        timestamp.add("");
        usericon.add(myfunction.user_profile_pic);
        Log.d("MYAPP my_msg", my_message);
        adapter.notifyDataSetChanged();
        */
    }

}
