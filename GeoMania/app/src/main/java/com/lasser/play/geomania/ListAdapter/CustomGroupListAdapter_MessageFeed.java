package com.lasser.play.geomania.ListAdapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.R;

import java.util.ArrayList;

/**
 * Created by antar on 06-Feb-17.
 */

public class CustomGroupListAdapter_MessageFeed extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<String> user;
    private final ArrayList<String> message;
    private final ArrayList<String> usericon;
    private final ArrayList<String> timestamp;
    private SharedFunctions myfunctions;
    public CustomGroupListAdapter_MessageFeed(Activity context, ArrayList<String> user, ArrayList<String> message, ArrayList<String> timestamp, ArrayList<String> usericon){
        super(context, R.layout.my_list_message_feed_from, user);
        // TODO Auto-generated constructor stub
        this.context=context;
        this.user = user;
        this.message = message;
        this.timestamp = timestamp;
        this.usericon = usericon;
        Log.d("MYAPP feedAdap","Init Adpater " + user.size());
    }
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        myfunctions = new SharedFunctions(context);
        Log.d("MYAPP adapter", "Inside ListAdapter_MessageFeed");
        if(user.get(position).equals(myfunctions.user)){
            View rowView=inflater.inflate(R.layout.my_list_message_feed_to, null,true);
            TextView tv_message = (TextView) rowView.findViewById(R.id.textView_to_message);
            TextView tv_user = (TextView) rowView.findViewById(R.id.textView_to_user);
            TextView tv_time = (TextView) rowView.findViewById(R.id.textView_to_timestamp);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView_to_usericon);
            tv_message.setText(message.get(position));
            tv_user.setText(user.get(position));
            tv_time.setText(timestamp.get(position));
            Log.d("MYAPP: adapter", "USER MESSAGE");
            imageView.setImageBitmap(myfunctions.setPicture(usericon.get(position),1));
            return rowView;
        }
        // Other Type of Messages
        View rowView=inflater.inflate(R.layout.my_list_message_feed_from, null,true);
        TextView tv_message = (TextView) rowView.findViewById(R.id.textView_from_message);
        TextView tv_user = (TextView) rowView.findViewById(R.id.textView_from_user);
        TextView tv_time = (TextView) rowView.findViewById(R.id.textView_from_timestamp);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView_from_usericon);
        tv_message.setText(message.get(position));
        tv_user.setText(user.get(position));
        tv_time.setText(timestamp.get(position));
        Log.d("MYAPP: adapter", "OTHER MESSAGE");
        imageView.setImageBitmap(myfunctions.setPicture(usericon.get(position),1));
        return rowView;
    };
}
