package com.lasser.play.geomania.ListAdapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.lasser.play.geomania.CustomDataStructure.MessageFeed;
import com.lasser.play.geomania.CustomDataStructure.SharedFunctions;
import com.lasser.play.geomania.R;

import java.util.ArrayList;

/**
 * Created by antar on 06-Feb-17.
 */

public class CustomGroupListAdapter_MessageFeed extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList<MessageFeed> messageFeeds;
    private SharedFunctions myfunctions;
    public CustomGroupListAdapter_MessageFeed(Activity context, ArrayList<MessageFeed> messageFeeds) {
        super(context, R.layout.my_list_message_feed_from);
        // TODO Auto-generated constructor stub
        this.context=context;
        this.messageFeeds = messageFeeds;
    }
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        if(messageFeeds.get(position).message_type == 2){
            View rowView=inflater.inflate(R.layout.my_list_message_feed_to, null,true);
            TextView tv_message = (TextView) rowView.findViewById(R.id.textView_to_message);
            TextView tv_user = (TextView) rowView.findViewById(R.id.textView_to_user);
            TextView tv_time = (TextView) rowView.findViewById(R.id.textView_to_timestamp);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView_to_usericon);
            tv_message.setText(messageFeeds.get(position).messages);
            tv_user.setText(messageFeeds.get(position).user);
            tv_time.setText(messageFeeds.get(position).timestamp);
            if(messageFeeds.get(position).usericon == "null"){
                imageView.setImageResource(R.mipmap.ic_launcher);
            }
            else{
                imageView.setImageBitmap(myfunctions.resizeBitmap(messageFeeds.get(position).usericon));
            }
            return rowView;
        }
        // Other Type of Messages
        View rowView=inflater.inflate(R.layout.my_list_message_feed_from, null,true);
        TextView tv_message = (TextView) rowView.findViewById(R.id.textView_from_message);
        TextView tv_user = (TextView) rowView.findViewById(R.id.textView_from_user);
        TextView tv_time = (TextView) rowView.findViewById(R.id.textView_from_timestamp);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView_from_usericon);
        tv_message.setText(messageFeeds.get(position).messages);
        tv_user.setText(messageFeeds.get(position).user);
        tv_time.setText(messageFeeds.get(position).timestamp);
        if(messageFeeds.get(position).usericon == "null"){
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            imageView.setImageBitmap(myfunctions.resizeBitmap(messageFeeds.get(position).usericon));
        }
        return rowView;
    };
}
