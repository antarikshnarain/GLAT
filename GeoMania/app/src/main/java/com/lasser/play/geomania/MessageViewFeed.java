package com.lasser.play.geomania;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.lasser.play.geomania.AsyncJava.nodeHttpRequest;
import com.lasser.play.geomania.CustomDataStructure.MapMessages;
import com.lasser.play.geomania.CustomDataStructure.MessageFeed;
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
    String user, phone, token;
    String group_id, message_id;

    CustomGroupListAdapter_MessageFeed adapter;
    ArrayList<MessageFeed> messageFeeds;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view_feed);
        Intent intent = getIntent();
        group_id = intent.getStringExtra("gid");
        message_id = intent.getStringExtra("mid");
        listView_feed = (ListView) findViewById(R.id.listview_message_feed);
        editText_my_message = (EditText) findViewById(R.id.editText_my_message);
        SharedPreferences phoneDetails = getSharedPreferences("userdata", MODE_PRIVATE);
        user = phoneDetails.getString("user","");
        phone = phoneDetails.getString("phone", "");
        token = phoneDetails.getString("token", "");
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(":((())):");
        progressDialog.setMessage("Retrieving Messages ...");
        progressDialog.show();
        getMessages();
    }

    public void getMessages(){
        try{
            JSONObject json = new JSONObject();
            json.put("phone",phone);
            json.put("token",token);
            json.put("gid",group_id);
            json.put("mid",message_id);
            URLDataHash mydata = new URLDataHash();
            mydata.url = "192.168.43.231";
            mydata.apicall = "user/group/message";
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
                MessageFeed mFeed = new MessageFeed();
                mFeed.user = currobject.getString("user");
                mFeed.messages = currobject.getString("message");
                mFeed.message_type = currobject.getInt("readState");
                mFeed.timestamp = currobject.getString("createdAt");
                messageFeeds.add(i,mFeed);
            }
            adapter = new CustomGroupListAdapter_MessageFeed(this, messageFeeds);
            listView_feed.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
        catch (JSONException e ){ e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }
    }

    public void addMessageToFeed(View v){
        String my_message = editText_my_message.getText().toString();
        if (my_message.equals(""))
            return;
        MessageFeed myFeed = new MessageFeed();
        myFeed.user = user;
        myFeed.messages = my_message;
        myFeed.message_type = 2;
        messageFeeds.add(myFeed);
        adapter.notifyDataSetChanged();
    }
}
